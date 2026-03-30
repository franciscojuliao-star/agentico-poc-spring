package br.ufc.llm.aula.controller;

import br.ufc.llm.aula.domain.TipoArquivo;
import br.ufc.llm.aula.dto.AulaResponse;
import br.ufc.llm.aula.exception.AulaNaoEncontradaException;
import br.ufc.llm.aula.service.AulaService;
import br.ufc.llm.modulo.exception.ModuloNaoEncontradoException;
import br.ufc.llm.shared.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AulaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AulaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AulaService aulaService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 ao listar aulas do módulo (US-P20)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoListarAulas() throws Exception {
        when(aulaService.listar(eq(1L), any())).thenReturn(java.util.List.of(
                new AulaResponse(1L, "Aula 1", 1, null, null, null, null, 1L),
                new AulaResponse(2L, "Aula 2", 2, null, null, null, null, 1L)));

        mockMvc.perform(get("/modulos/1/aulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("Deve retornar 201 ao adicionar aula (US-P20)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoAdicionarAula() throws Exception {
        when(aulaService.adicionar(eq(1L), any(), any(), any())).thenReturn(
                new AulaResponse(1L, "Introdução ao Java", 1, null, null, null, null, 1L));

        var dados = dadosPart("{\"nome\": \"Introdução ao Java\"}");

        mockMvc.perform(multipart("/modulos/1/aulas").part(dados))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.nome").value("Introdução ao Java"))
                .andExpect(jsonPath("$.data.ordem").value(1));
    }

    @Test
    @DisplayName("Deve retornar 201 ao adicionar aula com arquivo (US-P21)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoAdicionarAulaComArquivo() throws Exception {
        when(aulaService.adicionar(eq(1L), any(), any(), any())).thenReturn(
                new AulaResponse(1L, "Aula com PDF", 1, "slides.pdf", TipoArquivo.PDF, null, null, 1L));

        var dados = dadosPart("{\"nome\": \"Aula com PDF\"}");
        var arquivo = new MockMultipartFile("arquivo", "slides.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/modulos/1/aulas").part(dados).file(arquivo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tipoArquivo").value("PDF"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando nome está em branco ao adicionar aula")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoNomeEmBrancoAoAdicionar() throws Exception {
        var dados = dadosPart("{\"nome\": \"\"}");

        mockMvc.perform(multipart("/modulos/1/aulas").part(dados))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 ao adicionar aula em módulo inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoAdicionarAulaModuloInexistente() throws Exception {
        doThrow(new ModuloNaoEncontradoException(99L)).when(aulaService).adicionar(eq(99L), any(), any(), any());

        var dados = dadosPart("{\"nome\": \"Aula\"}");

        mockMvc.perform(multipart("/modulos/99/aulas").part(dados))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 ao fazer upload de arquivo (US-P21)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoUploadArquivo() throws Exception {
        when(aulaService.atualizarArquivo(eq(1L), any(), any())).thenReturn(
                new AulaResponse(1L, "Aula 1", 1, "20260323_prof.pdf", TipoArquivo.PDF, null, null, 1L));

        var arquivo = new MockMultipartFile("arquivo", "slides.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PATCH, "/aulas/1/arquivo").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.tipoArquivo").value("PDF"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao fazer upload em aula inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoUploadAulaInexistente() throws Exception {
        doThrow(new AulaNaoEncontradaException(99L)).when(aulaService).atualizarArquivo(any(), any(), any());

        var arquivo = new MockMultipartFile("arquivo", "slides.pdf", "application/pdf", new byte[]{1});

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PATCH, "/aulas/99/arquivo").file(arquivo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 ao editar aula com conteúdo CKEditor (US-P22 e US-P23)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoEditarAula() throws Exception {
        when(aulaService.editar(eq(1L), any(), any())).thenReturn(
                new AulaResponse(1L, "Novo nome", 1, null, null, "<p>Conteúdo</p>", null, 1L));

        mockMvc.perform(put("/aulas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Novo nome\", \"conteudoCkEditor\": \"<p>Conteúdo</p>\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.conteudoCkEditor").value("<p>Conteúdo</p>"));
    }

    @Test
    @DisplayName("Deve retornar 204 ao excluir aula")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar204AoExcluirAula() throws Exception {
        doNothing().when(aulaService).excluir(any(), any());

        mockMvc.perform(delete("/aulas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 404 ao excluir aula inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoExcluirAulaInexistente() throws Exception {
        doThrow(new AulaNaoEncontradaException(99L)).when(aulaService).excluir(any(), any());

        mockMvc.perform(delete("/aulas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 ao reordenar aula (US-P24)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoReordenarAula() throws Exception {
        doNothing().when(aulaService).reordenar(any(), anyInt(), any());

        mockMvc.perform(patch("/aulas/1/ordem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaOrdem\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 404 ao reordenar aula inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoReordenarAulaInexistente() throws Exception {
        doThrow(new AulaNaoEncontradaException(99L)).when(aulaService).reordenar(any(), anyInt(), any());

        mockMvc.perform(patch("/aulas/99/ordem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novaOrdem\": 2}"))
                .andExpect(status().isNotFound());
    }

    private MockPart dadosPart(String json) {
        var part = new MockPart("dados", json.getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return part;
    }
}
