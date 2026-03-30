package br.ufc.llm.aula.controller;

import br.ufc.llm.aula.dto.ConteudoGeradoResponse;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.exception.ConteudoGeradoAusenteException;
import br.ufc.llm.aula.exception.ConteudoInsuficienteException;
import br.ufc.llm.aula.service.AulaIaService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

@WebMvcTest(AulaIaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AulaIaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AulaIaService aulaIaService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 com preview do conteúdo gerado pela IA (US-P25/P26)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoGerarConteudo() throws Exception {
        when(aulaIaService.gerarConteudo(any(), any())).thenReturn(
                new ConteudoGeradoResponse(1L, "<h1>Conteúdo gerado</h1>"));

        mockMvc.perform(post("/aulas/1/gerar-conteudo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.conteudoGerado").value("<h1>Conteúdo gerado</h1>"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao gerar conteúdo de aula inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoGerarConteudoAulaInexistente() throws Exception {
        doThrow(new AulaNaoEncontradaException(99L)).when(aulaIaService).gerarConteudo(any(), any());

        mockMvc.perform(post("/aulas/99/gerar-conteudo"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 422 quando aula não tem conteúdo para gerar (US-P25)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar422QuandoAulaSemConteudo() throws Exception {
        doThrow(new ConteudoInsuficienteException()).when(aulaIaService).gerarConteudo(any(), any());

        mockMvc.perform(post("/aulas/1/gerar-conteudo"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Deve retornar 200 ao confirmar conteúdo gerado (US-P27)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoConfirmarConteudo() throws Exception {
        when(aulaIaService.confirmarConteudo(any(), any(), any())).thenReturn(
                new ConteudoGeradoResponse(1L, "<h1>Confirmado</h1>"));

        mockMvc.perform(post("/aulas/1/confirmar-conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"<h1>Confirmado</h1>\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.conteudoGerado").value("<h1>Confirmado</h1>"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao confirmar sem enviar conteúdo no body")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400AoConfirmarSemBody() throws Exception {
        mockMvc.perform(post("/aulas/1/confirmar-conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 422 ao confirmar sem conteúdo gerado prévio")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar422AoConfirmarSemConteudoGerado() throws Exception {
        doThrow(new ConteudoGeradoAusenteException()).when(aulaIaService).confirmarConteudo(any(), any(), any());

        mockMvc.perform(post("/aulas/1/confirmar-conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"<h1>Conteúdo</h1>\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Deve retornar 404 ao confirmar conteúdo de aula inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoConfirmarConteudoAulaInexistente() throws Exception {
        doThrow(new AulaNaoEncontradaException(99L)).when(aulaIaService).confirmarConteudo(any(), any(), any());

        mockMvc.perform(post("/aulas/99/confirmar-conteudo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"<h1>Conteúdo</h1>\"}"))
                .andExpect(status().isNotFound());
    }
}
