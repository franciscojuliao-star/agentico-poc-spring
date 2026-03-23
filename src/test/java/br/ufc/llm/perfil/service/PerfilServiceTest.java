package br.ufc.llm.perfil.service;

import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private UsuarioRepository usuarioRepository;

    private PerfilService perfilService;

    @BeforeEach
    void setUp() {
        perfilService = new PerfilService(usuarioRepository, tempDir.toString());
    }

    @Test
    @DisplayName("Deve salvar foto e atualizar fotoPerfil do usuário")
    void deveAtualizarFotoComArquivoValido() {
        var usuario = usuario();
        when(usuarioRepository.findByEmail("prof@email.com")).thenReturn(Optional.of(usuario));

        var arquivo = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        perfilService.atualizarFoto(arquivo, "prof@email.com");

        assertThat(usuario.getFotoPerfil()).isNotBlank();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar TipoArquivoInvalidoException para arquivo não imagem")
    void deveLancarExcecaoParaArquivoNaoImagem() {
        var arquivo = new MockMultipartFile("foto", "documento.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});

        assertThatThrownBy(() -> perfilService.atualizarFoto(arquivo, "prof@email.com"))
                .isInstanceOf(TipoArquivoInvalidoException.class);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .email("prof@email.com")
                .nome("Professor Silva")
                .senha("hash")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.ATIVO)
                .build();
    }
}
