package br.ufc.llm.shared.security;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "chave-secreta-para-testes-unitarios-hmac256-minimo-32-bytes";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
    }

    @Test
    @DisplayName("Access token deve conter e-mail do usuário no subject")
    void deveGerarAccessTokenComEmailNoSubject() {
        var usuario = usuarioProfessor();
        String token = jwtService.gerarAccessToken(usuario);
        assertThat(jwtService.extrairEmail(token)).isEqualTo("prof@email.com");
    }

    @Test
    @DisplayName("Refresh token deve conter e-mail no subject")
    void deveGerarRefreshTokenComEmailNoSubject() {
        String token = jwtService.gerarRefreshToken("prof@email.com");
        assertThat(jwtService.extrairEmail(token)).isEqualTo("prof@email.com");
    }

    @Test
    @DisplayName("Access token deve ser válido como ACCESS")
    void deveValidarAccessTokenComTipoCorreto() {
        String token = jwtService.gerarAccessToken(usuarioProfessor());
        assertThat(jwtService.isAccessTokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh token não deve ser aceito como ACCESS token")
    void naoDeveValidarRefreshTokenComoAccessToken() {
        String token = jwtService.gerarRefreshToken("prof@email.com");
        assertThat(jwtService.isAccessTokenValido(token)).isFalse();
    }

    @Test
    @DisplayName("Refresh token deve ser válido como REFRESH")
    void deveValidarRefreshTokenComTipoCorreto() {
        String token = jwtService.gerarRefreshToken("prof@email.com");
        assertThat(jwtService.isRefreshTokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("Access token não deve ser aceito como REFRESH token")
    void naoDeveValidarAccessTokenComoRefreshToken() {
        String token = jwtService.gerarAccessToken(usuarioProfessor());
        assertThat(jwtService.isRefreshTokenValido(token)).isFalse();
    }

    @Test
    @DisplayName("Token expirado deve ser inválido")
    void deveRetornarFalseParaTokenExpirado() {
        var jwtExpirado = new JwtService();
        ReflectionTestUtils.setField(jwtExpirado, "secret", SECRET);
        ReflectionTestUtils.setField(jwtExpirado, "accessTokenExpiration", -1000L);
        ReflectionTestUtils.setField(jwtExpirado, "refreshTokenExpiration", 604800000L);

        String token = jwtExpirado.gerarAccessToken(usuarioProfessor());
        assertThat(jwtExpirado.isAccessTokenValido(token)).isFalse();
    }

    @Test
    @DisplayName("Token com assinatura adulterada deve ser inválido")
    void deveRetornarFalseParaTokenAdulterado() {
        String token = jwtService.gerarAccessToken(usuarioProfessor());
        String adulterado = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isAccessTokenValido(adulterado)).isFalse();
    }

    private Usuario usuarioProfessor() {
        return Usuario.builder()
                .id(1L)
                .nome("Professor Teste")
                .email("prof@email.com")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.ATIVO)
                .build();
    }
}
