package br.ufc.llm.usuario.service;

import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.dto.CadastroRequest;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import br.ufc.llm.usuario.exception.CpfJaCadastradoException;
import br.ufc.llm.usuario.exception.EmailJaCadastradoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new CpfJaCadastradoException(request.cpf());
        }

        var usuario = Usuario.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .status(StatusUsuario.INATIVO) // RN01
                .build();

        var salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getStatus(),
                usuario.getCriadoEm()
        );
    }
}
