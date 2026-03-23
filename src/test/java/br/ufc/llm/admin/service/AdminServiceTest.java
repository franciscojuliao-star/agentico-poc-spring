package br.ufc.llm.admin.service;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private AdminService adminService;

    @Test
    @DisplayName("Deve retornar lista com todos os usuários cadastrados")
    void deveListarTodosOsUsuarios() {
        var usuarios = List.of(
                usuario(1L, "Professor Silva", PerfilUsuario.PROFESSOR, StatusUsuario.INATIVO),
                usuario(2L, "Administrador", PerfilUsuario.ADMIN, StatusUsuario.ATIVO)
        );
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        var resultado = adminService.listarUsuarios();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nome()).isEqualTo("Professor Silva");
        assertThat(resultado.get(1).nome()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários")
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        var resultado = adminService.listarUsuarios();

        assertThat(resultado).isEmpty();
    }

    private Usuario usuario(Long id, String nome, PerfilUsuario perfil, StatusUsuario status) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .cpf("000.000.000-00")
                .email(nome.toLowerCase().replace(" ", "") + "@email.com")
                .senha("hash")
                .perfil(perfil)
                .status(status)
                .criadoEm(LocalDateTime.now())
                .build();
    }
}
