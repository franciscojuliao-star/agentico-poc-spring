package br.ufc.llm.auth.controller;

import br.ufc.llm.auth.exception.TokenInvalidoOuExpiradoException;
import br.ufc.llm.auth.service.AuthService;
import br.ufc.llm.auth.service.RecuperacaoSenhaService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import br.ufc.llm.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecuperacaoSenhaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private AuthService authService;
    @MockitoBean private RecuperacaoSenhaService recuperacaoSenhaService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 quando e-mail existe e token é enviado")
    void deveRetornar200AoSolicitarRecuperacao() throws Exception {
        doNothing().when(recuperacaoSenhaService).solicitarRecuperacao(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "prof@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 200 mesmo quando e-mail não está cadastrado (segurança)")
    void deveRetornar200QuandoEmailNaoExiste() throws Exception {
        doNothing().when(recuperacaoSenhaService).solicitarRecuperacao(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "naoexiste@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 400 quando e-mail está em branco")
    void deveRetornar400QuandoEmailEmBranco() throws Exception {
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando e-mail é inválido")
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "nao-eh-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 ao redefinir senha com token válido")
    void deveRetornar200AoRedefinirSenhaComTokenValido() throws Exception {
        doNothing().when(recuperacaoSenhaService).redefinirSenha(any(), any());

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-uuid",
                                "novaSenha", "novaSenha123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 400 quando token é inválido ou expirado")
    void deveRetornar400QuandoTokenInvalidoOuExpirado() throws Exception {
        doThrow(new TokenInvalidoOuExpiradoException()).when(recuperacaoSenhaService).redefinirSenha(any(), any());

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-invalido",
                                "novaSenha", "novaSenha123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar 400 quando token está em branco")
    void deveRetornar400QuandoTokenEmBranco() throws Exception {
        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "",
                                "novaSenha", "novaSenha123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando nova senha está em branco")
    void deveRetornar400QuandoNovaSenhaEmBranco() throws Exception {
        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-uuid",
                                "novaSenha", ""
                        ))))
                .andExpect(status().isBadRequest());
    }
}
