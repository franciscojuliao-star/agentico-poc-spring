package br.ufc.llm.auth.service;

import br.ufc.llm.auth.dto.LoginRequest;
import br.ufc.llm.auth.exception.ContaInativaException;
import br.ufc.llm.auth.exception.CredenciaisInvalidasException;
import br.ufc.llm.shared.security.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("Deve retornar access e refresh token ao fazer login com sucesso")
    void deveRealizarLoginComSucesso() {
        var request = new LoginRequest("prof@email.com", "senha1234");
        var usuario = usuarioAtivo();

        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", "hash")).thenReturn(true);
        when(jwtService.gerarAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.gerarRefreshToken("prof@email.com")).thenReturn("refresh-token");

        var response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando e-mail não existe")
    void deveLancarExcecaoQuandoEmailNaoEncontrado() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@x.com", "senha1234")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando senha incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuarioAtivo()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("prof@email.com", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("Deve lançar ContaInativaException quando conta está inativa (RN01)")
    void deveLancarExcecaoQuandoContaInativa() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(usuarioInativo()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("prof@email.com", "senha1234")))
                .isInstanceOf(ContaInativaException.class);
    }

    @Test
    @DisplayName("Deve renovar access token com refresh token válido")
    void deveRenovarAccessTokenComRefreshTokenValido() {
        when(jwtService.isRefreshTokenValido("refresh-token")).thenReturn(true);
        when(jwtService.extrairEmail("refresh-token")).thenReturn("prof@email.com");
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(usuarioAtivo()));
        when(jwtService.gerarAccessToken(any())).thenReturn("novo-access-token");

        var response = authService.refresh("refresh-token");

        assertThat(response.accessToken()).isEqualTo("novo-access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando refresh token é inválido")
    void deveLancarExcecaoQuandoRefreshTokenInvalido() {
        when(jwtService.isRefreshTokenValido("token-invalido")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("token-invalido"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("Deve lançar ContaInativaException no refresh quando conta foi desativada")
    void deveLancarExcecaoNoRefreshQuandoContaInativa() {
        when(jwtService.isRefreshTokenValido("refresh-token")).thenReturn(true);
        when(jwtService.extrairEmail("refresh-token")).thenReturn("prof@email.com");
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(usuarioInativo()));

        assertThatThrownBy(() -> authService.refresh("refresh-token"))
                .isInstanceOf(ContaInativaException.class);
    }

    private Usuario usuarioAtivo() {
        return Usuario.builder()
                .id(1L)
                .email("prof@email.com")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.ATIVO)
                .build();
    }

    private Usuario usuarioInativo() {
        return Usuario.builder()
                .id(1L)
                .email("prof@email.com")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.INATIVO)
                .build();
    }
}
