package br.ufc.llm.admin.service;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    @DisplayName("Deve ativar conta de usuário inativo")
    void deveAtivarContaDeUsuarioInativo() {
        var usuario = usuario(1L, "Professor Silva", PerfilUsuario.PROFESSOR, StatusUsuario.INATIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        adminService.ativar(1L);

        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException ao ativar id inexistente")
    void deveLancarExcecaoAoAtivarIdInexistente() {
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.ativar(99L))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve desativar conta de usuário ativo")
    void deveDesativarContaDeUsuarioAtivo() {
        var usuario = usuario(1L, "Professor Silva", PerfilUsuario.PROFESSOR, StatusUsuario.ATIVO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        adminService.desativar(1L);

        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.INATIVO);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException ao desativar id inexistente")
    void deveLancarExcecaoAoDesativarIdInexistente() {
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.desativar(99L))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
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
