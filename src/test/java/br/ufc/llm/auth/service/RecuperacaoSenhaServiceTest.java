package br.ufc.llm.auth.service;

import br.ufc.llm.auth.domain.TokenRecuperacaoSenha;
import br.ufc.llm.auth.exception.TokenInvalidoOuExpiradoException;
import br.ufc.llm.auth.repository.TokenRecuperacaoSenhaRepository;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenRecuperacaoSenhaRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JavaMailSender mailSender;

    @InjectMocks private RecuperacaoSenhaService recuperacaoSenhaService;

    @Test
    @DisplayName("Deve gerar token e enviar e-mail quando e-mail existe")
    void deveSolicitarRecuperacaoComSucessoQuandoEmailExiste() {
        var usuario = usuario();
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(usuario));

        recuperacaoSenhaService.solicitarRecuperacao("prof@email.com");

        verify(tokenRepository).invalidarTokensDoUsuario(usuario.getId());
        var captor = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isNotBlank();
        assertThat(captor.getValue().getUsuario()).isEqualTo(usuario);
        assertThat(captor.getValue().isUsado()).isFalse();
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Não deve lançar exceção quando e-mail não está cadastrado (segurança)")
    void deveSolicitarRecuperacaoSemErroQuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        recuperacaoSenhaService.solicitarRecuperacao("naoexiste@email.com");

        verify(tokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve redefinir senha e marcar token como usado quando token é válido")
    void deveRedefinirSenhaComTokenValido() {
        var usuario = usuario();
        var token = tokenValido(usuario);
        when(tokenRepository.findByToken("token-uuid")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-nova");

        recuperacaoSenhaService.redefinirSenha("token-uuid", "novaSenha123");

        assertThat(token.isUsado()).isTrue();
        assertThat(usuario.getSenha()).isEqualTo("hash-nova");
        verify(tokenRepository).save(token);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar TokenInvalidoOuExpiradoException quando token não existe")
    void deveLancarExcecaoQuandoTokenNaoEncontrado() {
        when(tokenRepository.findByToken(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recuperacaoSenhaService.redefinirSenha("invalido", "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }

    @Test
    @DisplayName("Deve lançar TokenInvalidoOuExpiradoException quando token está expirado")
    void deveLancarExcecaoQuandoTokenExpirado() {
        var usuario = usuario();
        var token = tokenExpirado(usuario);
        when(tokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> recuperacaoSenhaService.redefinirSenha("token-expirado", "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }

    @Test
    @DisplayName("Deve lançar TokenInvalidoOuExpiradoException quando token já foi usado")
    void deveLancarExcecaoQuandoTokenJaUsado() {
        var usuario = usuario();
        var token = tokenJaUsado(usuario);
        when(tokenRepository.findByToken("token-usado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> recuperacaoSenhaService.redefinirSenha("token-usado", "novaSenha123"))
                .isInstanceOf(TokenInvalidoOuExpiradoException.class);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .email("prof@email.com")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.ATIVO)
                .build();
    }

    private TokenRecuperacaoSenha tokenValido(Usuario usuario) {
        return TokenRecuperacaoSenha.builder()
                .token("token-uuid")
                .usuario(usuario)
                .expiradoEm(LocalDateTime.now().plusHours(1))
                .usado(false)
                .build();
    }

    private TokenRecuperacaoSenha tokenExpirado(Usuario usuario) {
        return TokenRecuperacaoSenha.builder()
                .token("token-expirado")
                .usuario(usuario)
                .expiradoEm(LocalDateTime.now().minusMinutes(1))
                .usado(false)
                .build();
    }

    private TokenRecuperacaoSenha tokenJaUsado(Usuario usuario) {
        return TokenRecuperacaoSenha.builder()
                .token("token-usado")
                .usuario(usuario)
                .expiradoEm(LocalDateTime.now().plusHours(1))
                .usado(true)
                .build();
    }
}
