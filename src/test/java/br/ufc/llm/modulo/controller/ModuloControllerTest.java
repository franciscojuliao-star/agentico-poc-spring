package br.ufc.llm.modulo.controller;

import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.modulo.dto.ModuloResponse;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.modulo.service.ModuloService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModuloController.class)
@AutoConfigureMockMvc(addFilters = false)
class ModuloControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ModuloService moduloService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 201 ao adicionar módulo (US-P16)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoAdicionarModulo() throws Exception {
        when(moduloService.adicionar(eq(1L), any())).thenReturn(
                new ModuloResponse(1L, "Módulo 01", 1, null, 1L));

        mockMvc.perform(post("/cursos/1/modulos"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.nome").value("Módulo 01"))
                .andExpect(jsonPath("$.data.ordem").value(1));
    }

    @Test
    @DisplayName("Deve retornar 404 ao adicionar módulo a curso inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoAdicionarModuloCursoInexistente() throws Exception {
        doThrow(new CursoNaoEncontradoException(99L)).when(moduloService).adicionar(eq(99L), any());

        mockMvc.perform(post("/cursos/99/modulos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 200 ao atualizar capa do módulo (US-P17)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoAtualizarCapa() throws Exception {
        when(moduloService.atualizarCapa(eq(1L), any(), any())).thenReturn(
                new ModuloResponse(1L, "Módulo 01", 1, "20260323_prof.jpg", 1L));

        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PATCH, "/modulos/1/capa").file(capa))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.capa").value("20260323_prof.jpg"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao atualizar capa de módulo inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoAtualizarCapaModuloInexistente() throws Exception {
        doThrow(new ModuloNaoEncontradoException(99L)).when(moduloService).atualizarCapa(any(), any(), any());

        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PATCH, "/modulos/99/capa").file(capa))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 204 ao excluir módulo (US-P18)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar204AoExcluirModulo() throws Exception {
        doNothing().when(moduloService).excluir(any(), any());

        mockMvc.perform(delete("/modulos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao excluir módulo inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoExcluirModuloInexistente() throws Exception {
        doThrow(new ModuloNaoEncontradoException(99L)).when(moduloService).excluir(any(), any());

        mockMvc.perform(delete("/modulos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 200 ao reordenar módulo (US-P19)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoReordenarModulo() throws Exception {
        doNothing().when(moduloService).reordenar(any(), anyInt(), any());

        mockMvc.perform(patch("/modulos/1/ordem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaOrdem\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 400 quando novaOrdem está ausente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoNovaOrdemAusente() throws Exception {
        mockMvc.perform(patch("/modulos/1/ordem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaOrdem\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 ao reordenar módulo inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoReordenarModuloInexistente() throws Exception {
        doThrow(new ModuloNaoEncontradoException(99L)).when(moduloService).reordenar(any(), anyInt(), any());

        mockMvc.perform(patch("/modulos/99/ordem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaOrdem\": 2}"))
                .andExpect(status().isNotFound());
    }
}
