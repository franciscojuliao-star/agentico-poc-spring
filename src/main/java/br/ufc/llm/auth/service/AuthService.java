package br.ufc.llm.auth.service;

import br.ufc.llm.auth.dto.LoginRequest;
import br.ufc.llm.auth.dto.LoginResponse;
import br.ufc.llm.auth.exception.ContaInativaException;
import br.ufc.llm.auth.exception.CredenciaisInvalidasException;
import br.ufc.llm.shared.security.JwtService;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        if (usuario.getStatus() == StatusUsuario.INATIVO) {
            throw new ContaInativaException();
        }

        return new LoginResponse(
                jwtService.gerarAccessToken(usuario),
                jwtService.gerarRefreshToken(usuario.getEmail())
        );
    }

    public LoginResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshTokenValido(refreshToken)) {
            throw new CredenciaisInvalidasException();
        }

        String email = jwtService.extrairEmail(refreshToken);

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (usuario.getStatus() == StatusUsuario.INATIVO) {
            throw new ContaInativaException();
        }

        return new LoginResponse(
                jwtService.gerarAccessToken(usuario),
                refreshToken
        );
    }
}
