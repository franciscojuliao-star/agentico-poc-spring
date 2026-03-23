package br.ufc.llm.curso.service;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.curso.dto.AlterarStatusRequest;
import br.ufc.llm.curso.dto.ConfigurarMatriculaRequest;
import br.ufc.llm.curso.dto.CriarCursoRequest;
import br.ufc.llm.curso.dto.EditarCursoRequest;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.exception.TransicaoStatusInvalidaException;
import br.ufc.llm.curso.repository.CursoRepository;

import java.util.List;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @TempDir
    Path tempDir;

    @Mock private CursoRepository cursoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private CursoService cursoService;

    @BeforeEach
    void setUp() {
        cursoService = new CursoService(cursoRepository, usuarioRepository, tempDir.toString());
    }

    @Test
    @DisplayName("Deve criar curso sem capa com status RASCUNHO")
    void deveCriarCursoSemCapaComSucesso() {
        var professor = professor();
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(professor));
        when(cursoRepository.save(any())).thenAnswer(inv -> {
            Curso c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new CriarCursoRequest("Curso de Java", "tecnologia", "Aprenda Java do zero", "40h");
        var response = cursoService.criar(request, null, "prof@email.com");

        assertThat(response.titulo()).isEqualTo("Curso de Java");
        assertThat(response.status()).isEqualTo(StatusCurso.RASCUNHO);
        assertThat(response.capa()).isNull();

        var captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoRepository).save(captor.capture());
        assertThat(captor.getValue().getProfessor()).isEqualTo(professor);
    }

    @Test
    @DisplayName("Deve criar curso com capa e salvar arquivo")
    void deveCriarCursoComCapaValida() {
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(professor()));
        when(cursoRepository.save(any())).thenAnswer(inv -> {
            Curso c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        var request = new CriarCursoRequest("Curso de Java", "tecnologia", "Aprenda Java do zero", "40h");
        var response = cursoService.criar(request, capa, "prof@email.com");

        assertThat(response.capa()).isNotBlank();
        assertThat(response.capa()).matches("\\d{14}_prof\\.jpg");
    }

    @Test
    @DisplayName("Deve normalizar categoria para minúsculas")
    void deveNormalizarCategoriaParaMinusculas() {
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(professor()));
        when(cursoRepository.save(any())).thenAnswer(inv -> {
            Curso c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        var request = new CriarCursoRequest("Curso de Java", "TECNOLOGIA", "Descrição", "40h");
        var response = cursoService.criar(request, null, "prof@email.com");

        assertThat(response.categoria()).isEqualTo("tecnologia");
    }

    @Test
    @DisplayName("Deve configurar dados de matrícula do curso")
    void deveConfigurarDadosDeMatricula() {
        var professor = professor();
        var curso = cursoDosProfessor(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any())).thenReturn(curso);

        var request = new ConfigurarMatriculaRequest(true, false, true);
        cursoService.configurarMatricula(1L, request, "prof@email.com");

        assertThat(curso.isRequerEndereco()).isTrue();
        assertThat(curso.isRequerGenero()).isFalse();
        assertThat(curso.isRequerIdade()).isTrue();
        verify(cursoRepository).save(curso);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException quando curso não existe")
    void deveLancarExcecaoQuandoCursoNaoEncontrado() {
        when(cursoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.configurarMatricula(99L, new ConfigurarMatriculaRequest(true, false, false), "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException quando professor não é dono do curso")
    void deveLancarExcecaoQuandoProfessorNaoEDono() {
        var outroProfessor = Usuario.builder().id(2L).email("outro@email.com").build();
        var curso = cursoDosProfessor(outroProfessor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.configurarMatricula(1L, new ConfigurarMatriculaRequest(true, false, false), "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve listar cursos separados em ativos e arquivados")
    void deveListarCursosSeparadosPorStatus() {
        var professor = professor();
        var cursoAtivo = cursoCom(professor, StatusCurso.PUBLICADO);
        var cursoRascunho = cursoCom(professor, StatusCurso.RASCUNHO);
        var cursoArquivado = cursoCom(professor, StatusCurso.ARQUIVADO);

        when(cursoRepository.findByProfessorEmailAndStatusIn("prof@email.com", List.of(StatusCurso.RASCUNHO, StatusCurso.PUBLICADO)))
                .thenReturn(List.of(cursoAtivo, cursoRascunho));
        when(cursoRepository.findByProfessorEmailAndStatusIn("prof@email.com", List.of(StatusCurso.ARQUIVADO)))
                .thenReturn(List.of(cursoArquivado));

        var resultado = cursoService.listar("prof@email.com");

        assertThat(resultado.ativos()).hasSize(2);
        assertThat(resultado.arquivados()).hasSize(1);
    }

    @Test
    @DisplayName("Deve publicar curso em RASCUNHO (US-P13)")
    void devePublicarCurso() {
        var professor = professor();
        var curso = cursoCom(professor, StatusCurso.RASCUNHO);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any())).thenReturn(curso);

        cursoService.alterarStatus(1L, new AlterarStatusRequest(StatusCurso.PUBLICADO), "prof@email.com");

        assertThat(curso.getStatus()).isEqualTo(StatusCurso.PUBLICADO);
        verify(cursoRepository).save(curso);
    }

    @Test
    @DisplayName("Deve arquivar curso publicado (US-P14)")
    void deveArquivarCurso() {
        var professor = professor();
        var curso = cursoCom(professor, StatusCurso.PUBLICADO);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any())).thenReturn(curso);

        cursoService.alterarStatus(1L, new AlterarStatusRequest(StatusCurso.ARQUIVADO), "prof@email.com");

        assertThat(curso.getStatus()).isEqualTo(StatusCurso.ARQUIVADO);
    }

    @Test
    @DisplayName("Deve lançar TransicaoStatusInvalidaException ao publicar curso arquivado")
    void deveLancarExcecaoAoPublicarCursoArquivado() {
        var professor = professor();
        var curso = cursoCom(professor, StatusCurso.ARQUIVADO);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.alterarStatus(1L, new AlterarStatusRequest(StatusCurso.PUBLICADO), "prof@email.com"))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Deve editar os dados de um curso existente")
    void deveEditarCursoComSucesso() {
        var professor = professor();
        var curso = cursoDosProfessor(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any())).thenReturn(curso);

        var request = new EditarCursoRequest("Novo Título", "programação", "Nova descrição", "60h");
        var response = cursoService.editar(1L, request, null, "prof@email.com");

        assertThat(response.titulo()).isEqualTo("Novo Título");
        assertThat(response.categoria()).isEqualTo("programação");
        assertThat(response.cargaHoraria()).isEqualTo("60h");
        verify(cursoRepository).save(curso);
    }

    @Test
    @DisplayName("Deve substituir capa ao editar com novo arquivo")
    void deveSubstituirCapaAoEditar() {
        var professor = professor();
        var curso = cursoDosProfessor(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(cursoRepository.save(any())).thenReturn(curso);

        var novaCapa = new MockMultipartFile("capa", "nova.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        var request = new EditarCursoRequest("Novo Título", "programação", "Nova descrição", "60h");
        var response = cursoService.editar(1L, request, novaCapa, "prof@email.com");

        assertThat(response.capa()).isNotBlank();
        assertThat(response.capa()).matches("\\d{14}_prof\\.jpg");
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException ao editar curso de outro professor")
    void deveLancarExcecaoAoEditarCursoDeOutroProfessor() {
        var outroProfessor = Usuario.builder().id(2L).email("outro@email.com").build();
        var curso = cursoDosProfessor(outroProfessor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.editar(1L, new EditarCursoRequest("T", "c", "d", "10h"), null, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve retornar cursos cujo título, descrição ou categoria contenham o termo")
    void deveBuscarCursosPorTexto() {
        var professor = professor();
        var curso = cursoCom(professor, StatusCurso.PUBLICADO);
        when(cursoRepository.buscarPorTexto("java", "prof@email.com")).thenReturn(List.of(curso));

        var resultado = cursoService.buscar("java", "prof@email.com");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).titulo()).isEqualTo("Curso");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum curso corresponde ao termo")
    void deveRetornarListaVaziaQuandoNenhumCursoCorresponde() {
        when(cursoRepository.buscarPorTexto("xyz", "prof@email.com")).thenReturn(List.of());

        var resultado = cursoService.buscar("xyz", "prof@email.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve excluir curso com sucesso (US-P15)")
    void deveExcluirCursoComSucesso() {
        var professor = professor();
        var curso = cursoDosProfessor(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        cursoService.excluir(1L, "prof@email.com");

        verify(cursoRepository).delete(curso);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException ao excluir curso inexistente")
    void deveLancarExcecaoAoExcluirCursoInexistente() {
        when(cursoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.excluir(99L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException ao excluir curso de outro professor")
    void deveLancarExcecaoAoExcluirCursoDeOutroProfessor() {
        var outroProfessor = Usuario.builder().id(2L).email("outro@email.com").build();
        var curso = cursoDosProfessor(outroProfessor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> cursoService.excluir(1L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    private Curso cursoCom(Usuario professor, StatusCurso status) {
        return Curso.builder()
                .id(1L)
                .titulo("Curso")
                .categoria("tecnologia")
                .descricao("Descrição")
                .cargaHoraria("40h")
                .status(status)
                .professor(professor)
                .build();
    }

    private Curso cursoDosProfessor(Usuario professor) {
        return Curso.builder()
                .id(1L)
                .titulo("Curso de Java")
                .categoria("tecnologia")
                .descricao("Descrição")
                .cargaHoraria("40h")
                .professor(professor)
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
