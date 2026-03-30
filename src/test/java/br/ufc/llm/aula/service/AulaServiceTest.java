package br.ufc.llm.aula.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.dto.CriarAulaRequest;
import br.ufc.llm.aula.dto.EditarAulaRequest;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.repository.AulaRepository;
import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.modulo.domain.Modulo;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @TempDir
    Path tempDir;

    @Mock private AulaRepository aulaRepository;
    @Mock private ModuloRepository moduloRepository;

    private AulaService aulaService;

    @BeforeEach
    void setUp() {
        aulaService = new AulaService(aulaRepository, moduloRepository, tempDir.toString());
    }

    @Test
    @DisplayName("Deve adicionar aula com nome informado e ordem incrementada (US-P20)")
    void deveAdicionarAulaComSucesso() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.countByModuloId(1L)).thenReturn(0);
        when(aulaRepository.save(any())).thenAnswer(inv -> {
            Aula a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        var request = new CriarAulaRequest("Introdução ao Java", null);
        var response = aulaService.adicionar(1L, request, null, "prof@email.com");

        assertThat(response.nome()).isEqualTo("Introdução ao Java");
        assertThat(response.ordem()).isEqualTo(1);
        assertThat(response.moduloId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao adicionar aula em módulo inexistente")
    void deveLancarExcecaoAoAdicionarAulaModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.adicionar(99L, new CriarAulaRequest("Aula", null), null, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve listar aulas de um módulo ordenadas (US-P20)")
    void deveListarAulasDoModulo() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.findByModuloIdOrderByOrdem(1L))
                .thenReturn(List.of(aula(1), aula(2)));

        var resultado = aulaService.listar(1L, "prof@email.com");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).ordem()).isEqualTo(1);
        assertThat(resultado.get(1).ordem()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao listar aulas de módulo inexistente")
    void deveLancarExcecaoAoListarAulasModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.listar(99L, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve adicionar aula com arquivo PDF na criação (US-P21)")
    void deveAdicionarAulaComArquivoPdf() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.countByModuloId(1L)).thenReturn(0);
        when(aulaRepository.save(any())).thenAnswer(inv -> {
            Aula a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        var request = new CriarAulaRequest("Aula com PDF", null);
        var arquivo = new MockMultipartFile("arquivo", "slides.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});

        var response = aulaService.adicionar(1L, request, arquivo, "prof@email.com");

        assertThat(response.arquivo()).isNotBlank();
        assertThat(response.tipoArquivo()).isEqualTo(TipoArquivo.PDF);
    }

    @Test
    @DisplayName("Deve adicionar aula com conteúdo CKEditor na criação (US-P22)")
    void deveAdicionarAulaComConteudoCkEditor() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.countByModuloId(1L)).thenReturn(0);
        when(aulaRepository.save(any())).thenAnswer(inv -> {
            Aula a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        var request = new CriarAulaRequest("Aula com texto", "<p>Conteúdo</p>");
        var response = aulaService.adicionar(1L, request, null, "prof@email.com");

        assertThat(response.conteudoCkEditor()).isEqualTo("<p>Conteúdo</p>");
        assertThat(response.arquivo()).isNull();
    }

    @Test
    @DisplayName("Deve fazer upload de arquivo PDF para a aula (US-P21)")
    void deveUploadArquivoPdf() {
        var aula = aula(1);
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));
        when(aulaRepository.save(any())).thenReturn(aula);

        var arquivo = new MockMultipartFile("arquivo", "slides.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});

        var response = aulaService.atualizarArquivo(1L, arquivo, "prof@email.com");

        assertThat(response.arquivo()).isNotBlank();
        assertThat(response.tipoArquivo()).isEqualTo(TipoArquivo.PDF);
    }

    @Test
    @DisplayName("Deve fazer upload de arquivo de vídeo para a aula (US-P21)")
    void deveUploadArquivoVideo() {
        var aula = aula(1);
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));
        when(aulaRepository.save(any())).thenReturn(aula);

        var arquivo = new MockMultipartFile("arquivo", "aula.mp4", "video/mp4", new byte[]{0, 0, 0, 0x20, 0x66, 0x74, 0x79, 0x70});

        var response = aulaService.atualizarArquivo(1L, arquivo, "prof@email.com");

        assertThat(response.arquivo()).isNotBlank();
        assertThat(response.tipoArquivo()).isEqualTo(TipoArquivo.VIDEO);
    }

    @Test
    @DisplayName("Deve lançar AulaNaoEncontradaException ao fazer upload em aula inexistente")
    void deveLancarExcecaoAoUploadAulaInexistente() {
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        var arquivo = new MockMultipartFile("arquivo", "aula.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> aulaService.atualizarArquivo(99L, arquivo, "prof@email.com"))
                .isInstanceOf(AulaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve salvar conteúdo CKEditor na aula (US-P22 e US-P23)")
    void deveSalvarConteudoCkEditor() {
        var aula = aula(1);
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula));
        when(aulaRepository.save(any())).thenReturn(aula);

        var request = new EditarAulaRequest("Novo nome", "<p>Conteúdo</p>");
        var response = aulaService.editar(1L, request, "prof@email.com");

        assertThat(response.nome()).isEqualTo("Novo nome");
        assertThat(response.conteudoCkEditor()).isEqualTo("<p>Conteúdo</p>");
    }

    @Test
    @DisplayName("Deve excluir aula (US-P20)")
    void deveExcluirAula() {
        var aula = aula(2);
        var aula1 = aulaCom(1L, 1);
        var aula3 = aulaCom(3L, 3);

        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(2L, "prof@email.com"))
                .thenReturn(Optional.of(aula));
        when(aulaRepository.findByModuloIdOrderByOrdem(aula.getModulo().getId()))
                .thenReturn(new ArrayList<>(List.of(aula1, aula, aula3)));

        aulaService.excluir(2L, "prof@email.com");

        verify(aulaRepository).delete(aula);
        verify(aulaRepository).saveAll(any());
    }

    @Test
    @DisplayName("Deve lançar AulaNaoEncontradaException ao excluir aula inexistente")
    void deveLancarExcecaoAoExcluirAulaInexistente() {
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.excluir(99L, "prof@email.com"))
                .isInstanceOf(AulaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve reordenar aula dentro do módulo (US-P24)")
    void deveReordenarAula() {
        var aula1 = aulaCom(1L, 1);
        var aula2 = aulaCom(2L, 2);
        var aula3 = aulaCom(3L, 3);

        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(aula1));
        when(aulaRepository.findByModuloIdOrderByOrdem(aula1.getModulo().getId()))
                .thenReturn(new ArrayList<>(List.of(aula1, aula2, aula3)));

        aulaService.reordenar(1L, 3, "prof@email.com");

        verify(aulaRepository).saveAll(any());
    }

    @Test
    @DisplayName("Deve lançar AulaNaoEncontradaException ao reordenar aula inexistente")
    void deveLancarExcecaoAoReordenarAulaInexistente() {
        when(aulaRepository.findByIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aulaService.reordenar(99L, 2, "prof@email.com"))
                .isInstanceOf(AulaNaoEncontradaException.class);
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

    private Aula aula(int ordem) {
        return Aula.builder()
                .id((long) ordem)
                .nome("Aula " + ordem)
                .ordem(ordem)
                .modulo(modulo())
                .build();
    }

    private Aula aulaCom(Long id, int ordem) {
        return Aula.builder()
                .id(id)
                .nome("Aula " + ordem)
                .ordem(ordem)
                .modulo(modulo())
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
}
