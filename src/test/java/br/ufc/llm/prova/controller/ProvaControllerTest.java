package br.ufc.llm.prova.controller;

import br.ufc.llm.prova.dto.*;
import br.ufc.llm.prova.exception.*;
import br.ufc.llm.prova.service.ProvaService;
import br.ufc.llm.prova.service.QuizIaService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProvaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProvaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ProvaService provaService;
    @MockitoBean private QuizIaService quizIaService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 201 ao criar prova (US-P29)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoCriarProva() throws Exception {
        when(provaService.criar(any(), any())).thenReturn(
                new ProvaResponse(1L, 1L, false, false, false, List.of()));

        mockMvc.perform(post("/modulos/1/prova"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.moduloId").value(1));
    }

    @Test
    @DisplayName("Deve retornar 409 ao criar prova duplicada")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar409ProvaJaExiste() throws Exception {
        doThrow(new ProvaJaExisteException(1L)).when(provaService).criar(any(), any());

        mockMvc.perform(post("/modulos/1/prova"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 200 ao buscar prova do módulo")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoBuscarProva() throws Exception {
        when(provaService.buscar(any(), any())).thenReturn(
                new ProvaResponse(1L, 1L, false, false, false, List.of()));

        mockMvc.perform(get("/modulos/1/prova"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar prova inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoBuscarProvaInexistente() throws Exception {
        doThrow(new ProvaNaoEncontradaException(99L)).when(provaService).buscar(any(), any());

        mockMvc.perform(get("/modulos/99/prova"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 ao configurar prova (US-P32/P33/P34)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoConfigurarProva() throws Exception {
        when(provaService.configurar(any(), any(), any())).thenReturn(
                new ProvaResponse(1L, 1L, true, true, false, List.of()));

        mockMvc.perform(put("/provas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mostrarRespostasErradas\":true,\"mostrarRespostasCorretas\":true,\"mostrarValores\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mostrarRespostasErradas").value(true));
    }

    @Test
    @DisplayName("Deve retornar 204 ao excluir prova")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar204AoExcluirProva() throws Exception {
        doNothing().when(provaService).excluir(any(), any());

        mockMvc.perform(delete("/provas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 201 ao adicionar pergunta (US-P30/P31)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoAdicionarPergunta() throws Exception {
        when(provaService.adicionarPergunta(any(), any(), any())).thenReturn(
                new PerguntaResponse(1L, "Qual é Java?", 2, 1,
                        List.of(new AlternativaResponse(1L, "Linguagem", true),
                                new AlternativaResponse(2L, "Framework", false))));

        var body = """
                {"enunciado":"Qual é Java?","pontos":2,"alternativas":[
                  {"texto":"Linguagem","correta":true},{"texto":"Framework","correta":false}
                ]}""";

        mockMvc.perform(post("/provas/1/perguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.enunciado").value("Qual é Java?"))
                .andExpect(jsonPath("$.data.pontos").value(2))
                .andExpect(jsonPath("$.data.alternativas").isArray());
    }

    @Test
    @DisplayName("Deve retornar 400 quando enunciado está vazio")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400EnunciadoVazio() throws Exception {
        mockMvc.perform(post("/provas/1/perguntas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enunciado\":\"\",\"pontos\":1,\"alternativas\":[{\"texto\":\"A\",\"correta\":true}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 ao editar pergunta")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoEditarPergunta() throws Exception {
        when(provaService.editarPergunta(any(), any(), any())).thenReturn(
                new PerguntaResponse(1L, "Novo enunciado?", 3, 1, List.of()));

        mockMvc.perform(put("/perguntas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enunciado\":\"Novo enunciado?\",\"pontos\":3,\"alternativas\":[{\"texto\":\"A\",\"correta\":true}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enunciado").value("Novo enunciado?"));
    }

    @Test
    @DisplayName("Deve retornar 204 ao excluir pergunta")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar204AoExcluirPergunta() throws Exception {
        doNothing().when(provaService).excluirPergunta(any(), any());

        mockMvc.perform(delete("/perguntas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 201 ao adicionar alternativa")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoAdicionarAlternativa() throws Exception {
        when(provaService.adicionarAlternativa(any(), any(), any())).thenReturn(
                new AlternativaResponse(1L, "Opção A", false));

        mockMvc.perform(post("/perguntas/1/alternativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Opção A\",\"correta\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.texto").value("Opção A"));
    }

    @Test
    @DisplayName("Deve retornar 200 ao editar alternativa")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoEditarAlternativa() throws Exception {
        when(provaService.editarAlternativa(any(), any(), any())).thenReturn(
                new AlternativaResponse(1L, "Editada", true));

        mockMvc.perform(put("/alternativas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Editada\",\"correta\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correta").value(true));
    }

    @Test
    @DisplayName("Deve retornar 204 ao excluir alternativa")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar204AoExcluirAlternativa() throws Exception {
        doNothing().when(provaService).excluirAlternativa(any(), any());

        mockMvc.perform(delete("/alternativas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 200 com quiz gerado pela IA (US-P35/P36)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200ComQuizGerado() throws Exception {
        when(quizIaService.gerarQuiz(any(), any())).thenReturn(
                new QuizGeradoResponse(List.of(
                        new PerguntaResponse(null, "O que é Java?", 1, 0,
                                List.of(new AlternativaResponse(null, "Linguagem", true))))));

        mockMvc.perform(post("/modulos/1/prova/gerar-quiz-ia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.perguntas").isArray())
                .andExpect(jsonPath("$.data.perguntas[0].enunciado").value("O que é Java?"));
    }

    @Test
    @DisplayName("Deve retornar 200 ao salvar perguntas geradas (US-P37)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoSalvarPerguntasGeradas() throws Exception {
        doNothing().when(quizIaService).salvarPerguntas(any(), anyList(), any());

        var body = """
                [{"enunciado":"Pergunta?","pontos":1,"alternativas":[{"texto":"A","correta":true}]}]""";

        mockMvc.perform(post("/modulos/1/prova/salvar-quiz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
