package br.ufc.llm.prova.service;

import br.ufc.llm.aula.domain.Aula;
import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.aula.repository.AulaRepository;
import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.modulo.domain.Modulo;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
import br.ufc.llm.prova.domain.Prova;
import br.ufc.llm.prova.dto.PerguntaRequest;
import br.ufc.llm.prova.dto.QuizGeradoResponse;
import br.ufc.llm.prova.exception.ProvaNaoEncontradaException;
import br.ufc.llm.prova.exception.RespostaIaMalformadaException;
import br.ufc.llm.prova.repository.PerguntaRepository;
import br.ufc.llm.prova.repository.ProvaRepository;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizIaServiceTest {

    @TempDir
    Path tempDir;

    @Mock private ProvaRepository provaRepository;
    @Mock private PerguntaRepository perguntaRepository;
    @Mock private ModuloRepository moduloRepository;
    @Mock private AulaRepository aulaRepository;
    @Mock private ChatClient chatClient;

    private QuizIaService quizIaService;

    @BeforeEach
    void setUp() {
        quizIaService = new QuizIaService(
                provaRepository, perguntaRepository, moduloRepository,
                aulaRepository, chatClient, tempDir.toString(), new ObjectMapper());
    }

    @Test
    @DisplayName("Deve gerar quiz via IA sem salvar (US-P35/P36)")
    void deveGerarQuizSemSalvar() {
        var modulo = modulo();
        var aula = aulaComCkEditor(modulo, "<p>Conteúdo Java</p>");

        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.findByModuloIdOrderByOrdem(1L)).thenReturn(List.of(aula));

        mockChatClientJson("""
                [{"enunciado":"O que é Java?","pontos":1,"alternativas":[{"texto":"Linguagem de programação","correta":true},{"texto":"Framework","correta":false}]}]
                """);

        var response = quizIaService.gerarQuiz(1L, "prof@email.com");

        assertThat(response.perguntas()).hasSize(1);
        assertThat(response.perguntas().get(0).enunciado()).isEqualTo("O que é Java?");
        assertThat(response.perguntas().get(0).alternativas()).hasSize(2);
        verifyNoInteractions(provaRepository);
    }

    @Test
    @DisplayName("Deve gerar quiz mesmo sem prova criada previamente (US-P35)")
    void deveGerarQuizSemProvaCriada() {
        var modulo = modulo();
        var aula = aulaComCkEditor(modulo, "<p>Conteúdo</p>");

        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.findByModuloIdOrderByOrdem(1L)).thenReturn(List.of(aula));

        mockChatClientJson("""
                [{"enunciado":"Pergunta?","pontos":1,"alternativas":[{"texto":"A","correta":true},{"texto":"B","correta":false}]}]
                """);

        var response = quizIaService.gerarQuiz(1L, "prof@email.com");

        assertThat(response.perguntas()).hasSize(1);
        verifyNoInteractions(provaRepository);
    }

    @Test
    @DisplayName("Deve lançar RespostaIaMalformadaException quando IA retorna JSON inválido (US-P35)")
    void deveLancarExcecaoQuandoIaRetornaJsonInvalido() {
        var modulo = modulo();
        var aula = aulaComCkEditor(modulo, "<p>Conteúdo</p>");

        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.findByModuloIdOrderByOrdem(1L)).thenReturn(List.of(aula));

        mockChatClientJson("isso não é JSON válido");

        assertThatThrownBy(() -> quizIaService.gerarQuiz(1L, "prof@email.com"))
                .isInstanceOf(RespostaIaMalformadaException.class);
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao gerar quiz de módulo inexistente")
    void deveLancarExcecaoModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizIaService.gerarQuiz(99L, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar ConteudoInsuficienteException quando módulo não tem aulas com conteúdo")
    void deveLancarExcecaoSemConteudo() {
        var modulo = modulo();
        var aulaSemConteudo = Aula.builder().id(1L).nome("Aula").ordem(1).modulo(modulo).build();

        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(aulaRepository.findByModuloIdOrderByOrdem(1L)).thenReturn(List.of(aulaSemConteudo));

        assertThatThrownBy(() -> quizIaService.gerarQuiz(1L, "prof@email.com"))
                .isInstanceOf(ConteudoInsuficienteException.class);
    }

    @Test
    @DisplayName("Deve salvar perguntas geradas pela IA na prova (US-P37)")
    void deveSalvarPerguntasGeradasNaProva() {
        var modulo = modulo();
        var prova = prova(modulo);

        when(provaRepository.findByModuloIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(prova));
        when(perguntaRepository.countByProvaId(1L)).thenReturn(0);
        when(perguntaRepository.save(any())).thenAnswer(inv -> {
            br.ufc.llm.prova.domain.Pergunta p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var alternativas = List.of(
                new br.ufc.llm.prova.dto.AlternativaRequest("Certa", true),
                new br.ufc.llm.prova.dto.AlternativaRequest("Errada", false)
        );
        var perguntas = List.of(new PerguntaRequest("Pergunta gerada?", 1, alternativas));

        quizIaService.salvarPerguntas(1L, perguntas, "prof@email.com");

        verify(perguntaRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar ProvaNaoEncontradaException ao salvar perguntas em prova inexistente")
    void deveLancarExcecaoAoSalvarEmProvaInexistente() {
        when(provaRepository.findByModuloIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizIaService.salvarPerguntas(99L, List.of(), "prof@email.com"))
                .isInstanceOf(ProvaNaoEncontradaException.class);
    }

    @SuppressWarnings("unchecked")
    private void mockChatClientJson(String json) {
        var requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(json);
    }

    private Modulo modulo() {
        var professor = professor();
        var curso = Curso.builder()
                .id(1L).titulo("Curso").categoria("tecnologia")
                .descricao("Descrição").cargaHoraria("40h")
                .status(StatusCurso.RASCUNHO).professor(professor).build();
        return Modulo.builder().id(1L).nome("Módulo 01").ordem(1).curso(curso).build();
    }

    private Prova prova(Modulo modulo) {
        return Prova.builder().id(1L).modulo(modulo).build();
    }

    private Aula aulaComCkEditor(Modulo modulo, String conteudo) {
        return Aula.builder().id(1L).nome("Aula 1").ordem(1)
                .conteudoCkEditor(conteudo).modulo(modulo).build();
    }

    private Usuario professor() {
        return Usuario.builder()
                .id(1L).email("prof@email.com").nome("Professor Silva")
                .cpf("123.456.789-01").senha("hash")
                .perfil(PerfilUsuario.PROFESSOR).status(StatusUsuario.ATIVO).build();
    }
}
