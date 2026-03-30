package br.ufc.llm.auth.controller;

import br.ufc.llm.auth.dto.LoginResponse;
import br.ufc.llm.auth.exception.ContaInativaException;
import br.ufc.llm.auth.exception.CredenciaisInvalidasException;
import br.ufc.llm.auth.service.AuthService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 com tokens ao fazer login com sucesso")
    void deveRetornar200ComTokensNoLogin() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "prof@email.com",
                                "senha", "senha1234"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Deve retornar 401 com credenciais inválidas")
    void deveRetornar401ComCredenciaisInvalidas() throws Exception {
        when(authService.login(any())).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "prof@email.com",
                                "senha", "errada"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve retornar 403 quando conta está inativa (RN01)")
    void deveRetornar403QuandoContaInativa() throws Exception {
        when(authService.login(any())).thenThrow(new ContaInativaException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "prof@email.com",
                                "senha", "senha1234"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Deve retornar 400 quando e-mail está em branco")
    void deveRetornar400QuandoEmailEmBranco() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "",
                                "senha", "senha1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando e-mail é inválido")
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "nao-eh-email",
                                "senha", "senha1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando senha está em branco")
    void deveRetornar400QuandoSenhaEmBranco() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "prof@email.com",
                                "senha", ""
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 ao renovar token com refresh token válido")
    void deveRetornar200AoRenovarToken() throws Exception {
        when(authService.refresh("refresh-token")).thenReturn(new LoginResponse("novo-access-token", "refresh-token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("novo-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Deve retornar 401 quando refresh token é inválido")
    void deveRetornar401QuandoRefreshTokenInvalido() throws Exception {
        when(authService.refresh(any())).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "invalido"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve retornar 400 quando refreshToken está em branco")
    void deveRetornar400QuandoRefreshTokenEmBranco() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", ""))))
                .andExpect(status().isBadRequest());
    }
}
