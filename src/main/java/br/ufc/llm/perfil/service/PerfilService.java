package br.ufc.llm.perfil.service;

import br.ufc.llm.perfil.dto.PerfilResponse;
import br.ufc.llm.perfil.exception.SenhaAtualInvalidaException;
import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Slf4j
@Service
public class PerfilService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String uploadDir;

    public PerfilService(UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${upload.directory}") String uploadDir) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadDir = uploadDir;
    }

    public PerfilResponse buscarPerfil(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(0L));
        return toResponse(usuario);
    }

    public void alterarSenha(String email, String senhaAtual, String novaSenha) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(0L));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new SenhaAtualInvalidaException();
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    public void atualizarFoto(MultipartFile arquivo, String email) {
        validarImagem(arquivo);

        String nomeArquivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + extrairNomeUsuario(email)
                + extrairExtensao(arquivo.getOriginalFilename());
        Path destino = Path.of(uploadDir, nomeArquivo);

        try {
            Files.createDirectories(destino.getParent());
            arquivo.transferTo(destino);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a foto de perfil.", e);
        }

        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            excluirFotoAnterior(usuario.getFotoPerfil());
            usuario.setFotoPerfil(nomeArquivo);
            usuarioRepository.save(usuario);
        });
    }

    private void validarImagem(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new TipoArquivoInvalidoException();
        }
        try {
            String tipoDetectado = new Tika().detect(arquivo.getBytes());
            if (!TIPOS_PERMITIDOS.contains(tipoDetectado)) {
                throw new TipoArquivoInvalidoException();
            }
        } catch (IOException e) {
            throw new TipoArquivoInvalidoException();
        }
    }

    private void excluirFotoAnterior(String fotoPerfil) {
        if (fotoPerfil == null) return;
        try {
            Files.deleteIfExists(Path.of(uploadDir, fotoPerfil));
        } catch (IOException e) {
            log.warn("Não foi possível excluir foto anterior: {}", fotoPerfil);
        }
    }

    private PerfilResponse toResponse(Usuario usuario) {
        return new PerfilResponse(
                usuario.getId(),
                usuario.getNome(),
                mascararCpf(usuario.getCpf()),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.getStatus(),
                usuario.getFotoPerfil(),
                usuario.getCriadoEm()
        );
    }

    private String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() < 2) return cpf;
        return "***.***.***-" + cpf.substring(cpf.length() - 2);
    }

    private String extrairNomeUsuario(String email) {
        int arroba = email.indexOf('@');
        return arroba > 0 ? email.substring(0, arroba) : email;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) return ".jpg";
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }
}
