package br.ufc.llm.prova.service;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.modulo.domain.Modulo;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.repository.ModuloRepository;
import br.ufc.llm.prova.domain.Alternativa;
import br.ufc.llm.prova.domain.Pergunta;
import br.ufc.llm.prova.domain.Prova;
import br.ufc.llm.prova.dto.AlternativaRequest;
import br.ufc.llm.prova.dto.ConfigurarProvaRequest;
import br.ufc.llm.prova.dto.PerguntaRequest;
import br.ufc.llm.prova.exception.*;
import br.ufc.llm.prova.repository.AlternativaRepository;
import br.ufc.llm.prova.repository.PerguntaRepository;
import br.ufc.llm.prova.repository.ProvaRepository;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvaServiceTest {

    @Mock private ProvaRepository provaRepository;
    @Mock private PerguntaRepository perguntaRepository;
    @Mock private AlternativaRepository alternativaRepository;
    @Mock private ModuloRepository moduloRepository;

    private ProvaService provaService;

    @BeforeEach
    void setUp() {
        provaService = new ProvaService(provaRepository, perguntaRepository, alternativaRepository, moduloRepository);
    }

    @Test
    @DisplayName("Deve criar prova vinculada ao módulo (US-P29)")
    void deveCriarProva() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(provaRepository.existsByModuloId(1L)).thenReturn(false);
        when(provaRepository.save(any())).thenAnswer(inv -> {
            Prova p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var response = provaService.criar(1L, "prof@email.com");

        assertThat(response.moduloId()).isEqualTo(1L);
        assertThat(response.perguntas()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar ProvaJaExisteException ao criar prova duplicada")
    void deveLancarExcecaoProvaJaExiste() {
        var modulo = modulo();
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(provaRepository.existsByModuloId(1L)).thenReturn(true);

        assertThatThrownBy(() -> provaService.criar(1L, "prof@email.com"))
                .isInstanceOf(ProvaJaExisteException.class);
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao criar prova em módulo inexistente")
    void deveLancarExcecaoModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provaService.criar(99L, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve buscar prova por módulo (US-P29)")
    void deveBuscarProva() {
        var prova = prova();
        when(provaRepository.findByModuloIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(prova));

        var response = provaService.buscar(1L, "prof@email.com");

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar ProvaNaoEncontradaException ao buscar prova inexistente")
    void deveLancarExcecaoAoBuscarProvaInexistente() {
        when(provaRepository.findByModuloIdAndModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provaService.buscar(99L, "prof@email.com"))
                .isInstanceOf(ProvaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve configurar prova (US-P32/P33/P34)")
    void deveConfigurarProva() {
        var prova = prova();
        when(provaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(prova));
        when(provaRepository.save(any())).thenReturn(prova);

        var request = new ConfigurarProvaRequest(true, true, false);
        var response = provaService.configurar(1L, request, "prof@email.com");

        assertThat(response.mostrarRespostasErradas()).isTrue();
        assertThat(response.mostrarRespostasCorretas()).isTrue();
        assertThat(response.mostrarValores()).isFalse();
    }

    @Test
    @DisplayName("Deve excluir prova")
    void deveExcluirProva() {
        var prova = prova();
        when(provaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(prova));

        provaService.excluir(1L, "prof@email.com");

        verify(provaRepository).delete(prova);
    }

    @Test
    @DisplayName("Deve adicionar pergunta à prova (US-P30/P31)")
    void deveAdicionarPergunta() {
        var prova = prova();
        when(provaRepository.findByIdAndModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(prova));
        when(perguntaRepository.countByProvaId(1L)).thenReturn(0);
        when(perguntaRepository.save(any())).thenAnswer(inv -> {
            Pergunta p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var alternativas = List.of(
                new AlternativaRequest("Opção A", true),
                new AlternativaRequest("Opção B", false)
        );
        var request = new PerguntaRequest("Qual é o resultado de 2+2?", 2, alternativas);

        var response = provaService.adicionarPergunta(1L, request, "prof@email.com");

        assertThat(response.enunciado()).isEqualTo("Qual é o resultado de 2+2?");
        assertThat(response.pontos()).isEqualTo(2);
        assertThat(response.alternativas()).hasSize(2);
        assertThat(response.ordem()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve editar pergunta existente (US-P30)")
    void deveEditarPergunta() {
        var pergunta = pergunta();
        when(perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(pergunta));
        when(perguntaRepository.save(any())).thenReturn(pergunta);

        var alternativas = List.of(new AlternativaRequest("Nova opção", true));
        var request = new PerguntaRequest("Novo enunciado?", 3, alternativas);

        var response = provaService.editarPergunta(1L, request, "prof@email.com");

        assertThat(response.enunciado()).isEqualTo("Novo enunciado?");
        assertThat(response.pontos()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lançar PerguntaNaoEncontradaException ao editar pergunta inexistente")
    void deveLancarExcecaoAoEditarPerguntaInexistente() {
        when(perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        var request = new PerguntaRequest("Enunciado", 1, List.of(new AlternativaRequest("A", true)));

        assertThatThrownBy(() -> provaService.editarPergunta(99L, request, "prof@email.com"))
                .isInstanceOf(PerguntaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve excluir pergunta")
    void deveExcluirPergunta() {
        var pergunta = pergunta();
        when(perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(pergunta));

        provaService.excluirPergunta(1L, "prof@email.com");

        verify(perguntaRepository).delete(pergunta);
    }

    @Test
    @DisplayName("Deve adicionar alternativa à pergunta")
    void deveAdicionarAlternativa() {
        var pergunta = pergunta();
        when(perguntaRepository.findByIdAndProvaModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(pergunta));
        when(alternativaRepository.save(any())).thenAnswer(inv -> {
            Alternativa a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        var request = new AlternativaRequest("Nova alternativa", false);
        var response = provaService.adicionarAlternativa(1L, request, "prof@email.com");

        assertThat(response.texto()).isEqualTo("Nova alternativa");
        assertThat(response.correta()).isFalse();
    }

    @Test
    @DisplayName("Deve editar alternativa")
    void deveEditarAlternativa() {
        var alternativa = alternativa();
        when(alternativaRepository.findByIdAndPerguntaProvaModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(alternativa));
        when(alternativaRepository.save(any())).thenReturn(alternativa);

        var request = new AlternativaRequest("Texto editado", true);
        var response = provaService.editarAlternativa(1L, request, "prof@email.com");

        assertThat(response.texto()).isEqualTo("Texto editado");
        assertThat(response.correta()).isTrue();
    }

    @Test
    @DisplayName("Deve excluir alternativa")
    void deveExcluirAlternativa() {
        var alternativa = alternativa();
        when(alternativaRepository.findByIdAndPerguntaProvaModuloCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(alternativa));

        provaService.excluirAlternativa(1L, "prof@email.com");

        verify(alternativaRepository).delete(alternativa);
    }

    // --- helpers ---

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
        return Modulo.builder().id(1L).nome("Módulo 01").ordem(1).curso(curso).build();
    }

    private Prova prova() {
        return Prova.builder().id(1L).modulo(modulo()).build();
    }

    private Pergunta pergunta() {
        return Pergunta.builder()
                .id(1L)
                .enunciado("Pergunta?")
                .pontos(1)
                .ordem(1)
                .prova(prova())
                .build();
    }

    private Alternativa alternativa() {
        return Alternativa.builder()
                .id(1L)
                .texto("Opção A")
                .correta(false)
                .pergunta(pergunta())
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
