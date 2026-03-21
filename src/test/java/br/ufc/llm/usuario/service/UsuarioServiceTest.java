package br.ufc.llm.usuario.service;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.domain.Usuario;
import br.ufc.llm.usuario.dto.CadastroRequest;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import br.ufc.llm.usuario.exception.CpfJaCadastradoException;
import br.ufc.llm.usuario.exception.EmailJaCadastradoException;
import br.ufc.llm.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private CadastroRequest requestValido() {
        return new CadastroRequest(
                "João Silva",
                "123.456.789-09",
                "joao@email.com",
                "senha1234",
                PerfilUsuario.PROFESSOR
        );
    }

    private Usuario usuarioSalvo() {
        return Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .cpf("123.456.789-09")
                .email("joao@email.com")
                .senha("$2a$10$hashedSenha")
                .perfil(PerfilUsuario.PROFESSOR)
                .status(StatusUsuario.INATIVO)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // RN01 — conta criada com status INATIVO
    @Test
    @DisplayName("Deve salvar usuario com status INATIVO ao cadastrar")
    void deveSalvarUsuarioComStatusInativo() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedSenha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo());

        UsuarioResponse response = usuarioService.cadastrar(requestValido());

        assertThat(response.status()).isEqualTo(StatusUsuario.INATIVO);
    }

    @Test
    @DisplayName("Deve criptografar a senha antes de persistir")
    void deveCriptografarSenha() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("$2a$10$hashedSenha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo());

        usuarioService.cadastrar(requestValido());

        verify(passwordEncoder).encode("senha1234");
        verify(usuarioRepository).save(argThat(u -> u.getSenha().equals("$2a$10$hashedSenha")));
    }

    @Test
    @DisplayName("Deve lançar EmailJaCadastradoException quando e-mail já existe")
    void deveLancarExcecaoQuandoEmailDuplicado() {
        when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(requestValido()))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar CpfJaCadastradoException quando CPF já existe")
    void deveLancarExcecaoQuandoCpfDuplicado() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf("123.456.789-09")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(requestValido()))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar os dados corretos do usuario cadastrado")
    void deveRetornarDadosCorretosAoCadastrar() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedSenha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo());

        UsuarioResponse response = usuarioService.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("João Silva");
        assertThat(response.email()).isEqualTo("joao@email.com");
        assertThat(response.perfil()).isEqualTo(PerfilUsuario.PROFESSOR);
    }
}
