package br.ufc.llm.auth.service;

import br.ufc.llm.auth.domain.TokenRecuperacaoSenha;
import br.ufc.llm.auth.exception.TokenInvalidoOuExpiradoException;
import br.ufc.llm.auth.repository.TokenRecuperacaoSenhaRepository;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public void solicitarRecuperacao(String email) {
        var optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) return;

        var usuario = optUsuario.get();
        tokenRepository.invalidarTokensDoUsuario(usuario.getId());

        var token = TokenRecuperacaoSenha.builder()
                .token(UUID.randomUUID().toString())
                .usuario(usuario)
                .expiradoEm(LocalDateTime.now().plusHours(1))
                .build();
        tokenRepository.save(token);

        enviarEmail(usuario.getEmail(), token.getToken());
    }

    public void redefinirSenha(String tokenValor, String novaSenha) {
        var token = tokenRepository.findByToken(tokenValor)
                .orElseThrow(TokenInvalidoOuExpiradoException::new);

        if (token.isUsado() || token.getExpiradoEm().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoOuExpiradoException();
        }

        token.setUsado(true);
        tokenRepository.save(token);

        var usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    private void enviarEmail(String email, String token) {
        var mensagem = new SimpleMailMessage();
        mensagem.setTo(email);
        mensagem.setSubject("Recuperação de senha — PoC LLM UFC");
        mensagem.setText("""
                Você solicitou a recuperação de senha.

                Use o token abaixo para redefinir sua senha:

                %s

                Este token expira em 1 hora. Se não foi você, ignore este e-mail.
                """.formatted(token));
        mailSender.send(mensagem);
    }
}
