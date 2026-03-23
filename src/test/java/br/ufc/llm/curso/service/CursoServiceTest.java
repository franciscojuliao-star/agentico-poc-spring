package br.ufc.llm.curso.service;

import br.ufc.llm.curso.domain.Curso;
import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.curso.dto.CriarCursoRequest;
import br.ufc.llm.curso.repository.CursoRepository;
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
