package br.ufc.llm.aula.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.exception.ConteudoGeradoAusenteException;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.aula.repository.AulaRepository;
import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.modulo.domain.Modulo;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaIaServiceTest {

    @TempDir
    Path tempDir;

    @Mock private AulaRepository aulaRepository;
    @Mock private ChatClient chatClient;

    private AulaIaService aulaIaService;

    @BeforeEach
    void setUp() {
        aulaIaService = new AulaIaService(aulaRepository, chatClient, tempDir.toString());
    }

    @Test
    @DisplayName("Deve gerar conteúdo a partir do texto CKEditor (US-P25/P26)")
    void deveGerarConteudoAPartirDoCkEditor() {
        var aula = aulaComCkEditor("<p>Texto inicial</p>");
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));

        var promptCaptor = mockChatClientCall("<h1>Conteúdo gerado</h1>");

        when(aulaRepository.save(any())).thenReturn(aula);

        var response = aulaIaService.gerarConteudo(1L, "prof@email.com");

        assertThat(response.conteudoGerado()).isEqualTo("<h1>Conteúdo gerado</h1>");
        assertThat(aula.getConteudoGerado()).isEqualTo("<h1>Conteúdo gerado</h1>");
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("Deve gerar conteúdo a partir de arquivo PDF (US-P25/P26)")
    void deveGerarConteudoAPartirDoPdf() throws IOException {
        var pdfPath = tempDir.resolve("aula.pdf");
        Files.write(pdfPath, criarPdfMinimo());

        var aula = aulaComPdf(pdfPath.getFileName().toString());
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));

        mockChatClientCall("<h1>Conteúdo do PDF</h1>");

        when(aulaRepository.save(any())).thenReturn(aula);

        var response = aulaIaService.gerarConteudo(1L, "prof@email.com");

        assertThat(response.conteudoGerado()).isEqualTo("<h1>Conteúdo do PDF</h1>");
    }

    @Test
    @DisplayName("Deve lançar ConteudoInsuficienteException quando aula não tem PDF nem CKEditor")
    void deveLancarExcecaoQuandoSemConteudo() {
        var aula = aulaSemConteudo();
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));

        assertThatThrownBy(() -> aulaIaService.gerarConteudo(1L, "prof@email.com"))
                .isInstanceOf(ConteudoInsuficienteException.class);
    }

    @Test
    @DisplayName("Deve lançar AulaNaoEncontradaException ao gerar conteúdo de aula inexistente")
    void deveLancarExcecaoAulaInexistenteAoGerar() {
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaIaService.gerarConteudo(99L, "prof@email.com"))
                .isInstanceOf(AulaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve confirmar conteúdo gerado (US-P27)")
    void deveConfirmarConteudoGerado() {
        var aula = aulaComConteudoGerado("<h1>Gerado</h1>");
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));
        when(aulaRepository.save(any())).thenReturn(aula);

        var response = aulaIaService.confirmarConteudo(1L, "prof@email.com");

        assertThat(response.conteudoGerado()).isEqualTo("<h1>Gerado</h1>");
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("Deve lançar ConteudoGeradoAusenteException ao confirmar sem conteúdo gerado")
    void deveLancarExcecaoAoConfirmarSemConteudoGerado() {
        var aula = aulaSemConteudo();
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));

        assertThatThrownBy(() -> aulaIaService.confirmarConteudo(1L, "prof@email.com"))
                .isInstanceOf(ConteudoGeradoAusenteException.class);
    }

    @Test
    @DisplayName("Deve lançar AulaNaoEncontradaException ao confirmar conteúdo de aula inexistente")
    void deveLancarExcecaoAulaInexistenteAoConfirmar() {
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaIaService.confirmarConteudo(99L, "prof@email.com"))
                .isInstanceOf(AulaNaoEncontradaException.class);
    }

    // --- helpers ---

    @SuppressWarnings("unchecked")
    private ChatClient.ChatClientRequestSpec mockChatClientCall(String resposta) {
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(resposta);

        return requestSpec;
    }

    private Aula aulaComCkEditor(String conteudo) {
        return Aula.builder()
                .id(1L)
                .nome("Aula 1")
                .ordem(1)
                .conteudoCkEditor(conteudo)
                .modulo(modulo())
                .build();
    }

    private Aula aulaComPdf(String nomeArquivo) {
        return Aula.builder()
                .id(1L)
                .nome("Aula 1")
                .ordem(1)
                .arquivo(nomeArquivo)
                .tipoArquivo(TipoArquivo.PDF)
                .modulo(modulo())
                .build();
    }

    private Aula aulaSemConteudo() {
        return Aula.builder()
                .id(1L)
                .nome("Aula 1")
                .ordem(1)
                .modulo(modulo())
                .build();
    }

    private Aula aulaComConteudoGerado(String conteudo) {
        return Aula.builder()
                .id(1L)
                .nome("Aula 1")
                .ordem(1)
                .conteudoGerado(conteudo)
                .modulo(modulo())
                .build();
    }

    private Modulo modulo() {
        var professor = professor();
        var curso = Curso.builder()
                .id(1L)
                .titulo("Curso")
                .categoria("tecnologia")
                .descricao("Descrição")
                .cargaHoraria("40h")
                .status(StatusCurso.RASCUNHO)
                .professor(professor)
                .build();
        return Modulo.builder()
                .id(1L)
                .nome("Módulo 01")
                .ordem(1)
                .curso(curso)
                .build();
    }

    private Usuario professor() {
        return Usuario.builder()
                .id(1L)
                .email("prof@email.com")
                .nome("Professor Silva")
                .cpf("123.456.789-01")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.ATIVO)
                .build();
    }

    private byte[] criarPdfMinimo() {
        // PDF mínimo válido para PDFBox conseguir parsear
        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n" +
                "4 0 obj\n<< /Length 44 >>\nstream\nBT /F1 12 Tf 100 700 Td (Conteudo) Tj ET\nendstream\nendobj\n" +
                "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n" +
                "xref\n0 6\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000274 00000 n\n0000000370 00000 n\n" +
                "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n450\n%%EOF";
        return pdf.getBytes();
    }
}
