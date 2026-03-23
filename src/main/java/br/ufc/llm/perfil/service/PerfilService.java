package br.ufc.llm.perfil.service;

import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PerfilService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final UsuarioRepository usuarioRepository;
    private final String uploadDir;

    public PerfilService(UsuarioRepository usuarioRepository,
                         @Value("${upload.directory}") String uploadDir) {
        this.usuarioRepository = usuarioRepository;
        this.uploadDir = uploadDir;
    }

    public void atualizarFoto(MultipartFile arquivo, String email) {
        validarImagem(arquivo);

        String nomeArquivo = UUID.randomUUID() + extrairExtensao(arquivo.getOriginalFilename());
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

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) return ".jpg";
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }
}
