package br.ufc.llm.auth.controller;

import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import br.ufc.llm.auth.service.AuthService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import br.ufc.llm.usuario.exception.CpfJaCadastradoException;
import br.ufc.llm.usuario.exception.EmailJaCadastradoException;
import br.ufc.llm.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class CadastroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    private Map<String, String> bodyValido() {
        return Map.of(
                "nome", "João Silva",
                "cpf", "123.456.789-09",
                "email", "joao@email.com",
                "senha", "senha1234",
                "perfil", "PROFESSOR"
        );
    }

    private UsuarioResponse responseInativo() {
        return new UsuarioResponse(
                1L,
                "João Silva",
                "123.456.789-09",
                "joao@email.com",
                PerfilUsuario.PROFESSOR,
                StatusUsuario.INATIVO,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve retornar 201 ao cadastrar professor com dados válidos")
    void deveCadastrarProfessorComSucesso() throws Exception {
        when(usuarioService.cadastrar(any())).thenReturn(responseInativo());

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.data.status").value("INATIVO"));
    }

    // RN01 — status deve ser INATIVO na resposta
    @Test
    @DisplayName("Deve retornar status INATIVO na resposta (RN01)")
    void deveCriarContaComStatusInativo() throws Exception {
        when(usuarioService.cadastrar(any())).thenReturn(responseInativo());

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("INATIVO"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando nome está em branco")
    void deveRetornar400QuandoNomeVazio() throws Exception {
        var body = Map.of(
                "nome", "",
                "cpf", "123.456.789-09",
                "email", "joao@email.com",
                "senha", "senha1234",
                "perfil", "PROFESSOR"
        );

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando e-mail é inválido")
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        var body = Map.of(
                "nome", "João Silva",
                "cpf", "123.456.789-09",
                "email", "email-invalido",
                "senha", "senha1234",
                "perfil", "PROFESSOR"
        );

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando senha tem menos de 8 caracteres")
    void deveRetornar400QuandoSenhaCurta() throws Exception {
        var body = Map.of(
                "nome", "João Silva",
                "cpf", "123.456.789-09",
                "email", "joao@email.com",
                "senha", "abc",
                "perfil", "PROFESSOR"
        );

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando CPF não está no formato ###.###.###-##")
    void deveRetornar400QuandoCpfFormatoInvalido() throws Exception {
        var body = Map.of(
                "nome", "João Silva",
                "cpf", "12345678909",
                "email", "joao@email.com",
                "senha", "senha1234",
                "perfil", "PROFESSOR"
        );

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 409 quando e-mail já está cadastrado")
    void deveRetornar409QuandoEmailJaCadastrado() throws Exception {
        when(usuarioService.cadastrar(any())).thenThrow(new EmailJaCadastradoException("joao@email.com"));

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 409 quando CPF já está cadastrado")
    void deveRetornar409QuandoCpfJaCadastrado() throws Exception {
        when(usuarioService.cadastrar(any())).thenThrow(new CpfJaCadastradoException("123.456.789-09"));

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 400 quando perfil é ADMIN (não permitido no cadastro)")
    void deveRetornar400QuandoPerfilAdmin() throws Exception {
        var body = Map.of(
                "nome", "João Silva",
                "cpf", "123.456.789-09",
                "email", "joao@email.com",
                "senha", "senha1234",
                "perfil", "ADMIN"
        );

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
