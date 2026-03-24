package br.ufc.llm.modulo.service;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.repository.CursoRepository;
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
import org.mockito.ArgumentCaptor;
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
class ModuloServiceTest {

    @TempDir
    Path tempDir;

    @Mock private ModuloRepository moduloRepository;
    @Mock private CursoRepository cursoRepository;

    private ModuloService moduloService;

    @BeforeEach
    void setUp() {
        moduloService = new ModuloService(moduloRepository, cursoRepository, tempDir.toString());
    }

    @Test
    @DisplayName("Deve listar módulos de um curso ordenados (US-P16)")
    void deveListarModulosDoCurso() {
        var professor = professor();
        var curso = curso(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(moduloRepository.findByCursoIdOrderByOrdem(1L))
                .thenReturn(List.of(moduloCom(professor, 1L, 1), moduloCom(professor, 2L, 2)));

        var resultado = moduloService.listar(1L, "prof@email.com");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).ordem()).isEqualTo(1);
        assertThat(resultado.get(1).ordem()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException ao listar módulos de curso inexistente")
    void deveLancarExcecaoAoListarModulosCursoInexistente() {
        when(cursoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloService.listar(99L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException ao listar módulos de curso de outro professor")
    void deveLancarExcecaoAoListarModulosCursoDeOutroProfessor() {
        var outroProfessor = Usuario.builder().id(2L).email("outro@email.com").build();
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso(outroProfessor)));

        assertThatThrownBy(() -> moduloService.listar(1L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve adicionar módulo com nome gerado automaticamente (US-P16)")
    void deveAdicionarModuloComNomeGerado() {
        var professor = professor();
        var curso = curso(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(moduloRepository.countByCursoId(1L)).thenReturn(0);
        when(moduloRepository.save(any())).thenAnswer(inv -> {
            Modulo m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        var response = moduloService.adicionar(1L, "prof@email.com");

        assertThat(response.nome()).isEqualTo("Módulo 01");
        assertThat(response.ordem()).isEqualTo(1);
        assertThat(response.cursoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve gerar nome incremental para o segundo módulo")
    void deveGerarNomeIncrementalParaSegundoModulo() {
        var professor = professor();
        var curso = curso(professor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));
        when(moduloRepository.countByCursoId(1L)).thenReturn(2);
        when(moduloRepository.save(any())).thenAnswer(inv -> {
            Modulo m = inv.getArgument(0);
            m.setId(3L);
            return m;
        });

        var response = moduloService.adicionar(1L, "prof@email.com");

        assertThat(response.nome()).isEqualTo("Módulo 03");
        assertThat(response.ordem()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException quando curso não existe ao adicionar módulo")
    void deveLancarExcecaoQuandoCursoNaoEncontradoAoAdicionar() {
        when(cursoRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloService.adicionar(99L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar CursoNaoEncontradoException quando professor não é dono ao adicionar módulo")
    void deveLancarExcecaoQuandoProfessorNaoDonoCurso() {
        var outroProfessor = Usuario.builder().id(2L).email("outro@email.com").build();
        var curso = curso(outroProfessor);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso));

        assertThatThrownBy(() -> moduloService.adicionar(1L, "prof@email.com"))
                .isInstanceOf(CursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve salvar capa do módulo (US-P17)")
    void deveSalvarCapaDoModulo() {
        var professor = professor();
        var modulo = modulo(professor, 1);
        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(modulo));
        when(moduloRepository.save(any())).thenReturn(modulo);

        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        var response = moduloService.atualizarCapa(1L, capa, "prof@email.com");

        assertThat(response.capa()).isNotBlank();
        assertThat(response.capa()).matches("\\d{14}_prof\\.jpg");
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao atualizar capa de módulo inexistente")
    void deveLancarExcecaoAoAtualizarCapaModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg", new byte[]{1});

        assertThatThrownBy(() -> moduloService.atualizarCapa(99L, capa, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve excluir módulo e renumerar os demais (US-P18)")
    void deveExcluirModuloERenumerar() {
        var professor = professor();
        var modulo1 = moduloCom(professor, 1L, 1);
        var modulo2 = moduloCom(professor, 2L, 2);
        var modulo3 = moduloCom(professor, 3L, 3);

        when(moduloRepository.findByIdAndCursoProfessorEmail(2L, "prof@email.com"))
                .thenReturn(Optional.of(modulo2));
        when(moduloRepository.findByCursoIdOrderByOrdem(modulo2.getCurso().getId()))
                .thenReturn(new ArrayList<>(List.of(modulo1, modulo2, modulo3)));

        moduloService.excluir(2L, "prof@email.com");

        verify(moduloRepository).delete(modulo2);
        var captor = ArgumentCaptor.forClass(List.class);
        verify(moduloRepository).saveAll(captor.capture());

        @SuppressWarnings("unchecked")
        List<Modulo> salvos = captor.getValue();
        assertThat(salvos).hasSize(2);
        assertThat(salvos.get(0).getOrdem()).isEqualTo(1);
        assertThat(salvos.get(1).getOrdem()).isEqualTo(2);
        assertThat(salvos.get(1).getNome()).isEqualTo("Módulo 02");
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao excluir módulo inexistente")
    void deveLancarExcecaoAoExcluirModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloService.excluir(99L, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve reordenar módulo para nova posição (US-P19)")
    void deveReordenarModulo() {
        var professor = professor();
        var modulo1 = moduloCom(professor, 1L, 1);
        var modulo2 = moduloCom(professor, 2L, 2);
        var modulo3 = moduloCom(professor, 3L, 3);
        var alvo = moduloCom(professor, 1L, 1);

        when(moduloRepository.findByIdAndCursoProfessorEmail(1L, "prof@email.com"))
                .thenReturn(Optional.of(alvo));
        when(moduloRepository.findByCursoIdOrderByOrdem(alvo.getCurso().getId()))
                .thenReturn(new ArrayList<>(List.of(modulo1, modulo2, modulo3)));

        moduloService.reordenar(1L, 3, "prof@email.com");

        verify(moduloRepository).saveAll(any());
    }

    @Test
    @DisplayName("Deve lançar ModuloNaoEncontradoException ao reordenar módulo inexistente")
    void deveLancarExcecaoAoReordenarModuloInexistente() {
        when(moduloRepository.findByIdAndCursoProfessorEmail(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloService.reordenar(99L, 2, "prof@email.com"))
                .isInstanceOf(ModuloNaoEncontradoException.class);
    }

    private Curso curso(Usuario professor) {
        return Curso.builder()
                .id(1L)
                .titulo("Curso de Java")
                .categoria("tecnologia")
                .descricao("Descrição")
                .cargaHoraria("40h")
                .status(StatusCurso.RASCUNHO)
                .professor(professor)
                .build();
    }

    private Modulo modulo(Usuario professor, int ordem) {
        return Modulo.builder()
                .id(1L)
                .nome("Módulo 0" + ordem)
                .ordem(ordem)
                .curso(curso(professor))
                .build();
    }

    private Modulo moduloCom(Usuario professor, Long id, int ordem) {
        return Modulo.builder()
                .id(id)
                .nome("Módulo 0" + ordem)
                .ordem(ordem)
                .curso(curso(professor))
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
