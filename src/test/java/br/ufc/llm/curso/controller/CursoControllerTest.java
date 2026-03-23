package br.ufc.llm.curso.controller;

import br.ufc.llm.curso.domain.StatusCurso;
import br.ufc.llm.curso.dto.CursoResponse;
import br.ufc.llm.curso.dto.ListaCursosResponse;
import br.ufc.llm.curso.exception.CursoNaoEncontradoException;
import br.ufc.llm.curso.exception.TransicaoStatusInvalidaException;
import br.ufc.llm.curso.service.CursoService;
import br.ufc.llm.shared.security.JwtAuthFilter;
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

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CursoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CursoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CursoService cursoService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 201 ao criar curso sem capa")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoCriarCurso() throws Exception {
        when(cursoService.criar(any(), any(), any())).thenReturn(new CursoResponse(
                1L, "Curso de Java", "tecnologia", "Aprenda Java", "40h",
                null, StatusCurso.RASCUNHO, 1L, LocalDateTime.now()
        ));

        var dados = dadosPart("{\"titulo\":\"Curso de Java\",\"categoria\":\"tecnologia\",\"descricao\":\"Aprenda Java\",\"cargaHoraria\":\"40h\"}");

        mockMvc.perform(multipart("/cursos").part(dados))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.titulo").value("Curso de Java"))
                .andExpect(jsonPath("$.data.status").value("RASCUNHO"));
    }

    @Test
    @DisplayName("Deve retornar 201 ao criar curso com capa")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar201AoCriarCursoComCapa() throws Exception {
        when(cursoService.criar(any(), any(), any())).thenReturn(new CursoResponse(
                1L, "Curso de Java", "tecnologia", "Aprenda Java", "40h",
                "20260323_prof.jpg", StatusCurso.RASCUNHO, 1L, LocalDateTime.now()
        ));

        var dados = dadosPart("{\"titulo\":\"Curso de Java\",\"categoria\":\"tecnologia\",\"descricao\":\"Aprenda Java\",\"cargaHoraria\":\"40h\"}");
        var capa = new MockMultipartFile("capa", "capa.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/cursos").part(dados).file(capa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.capa").value("20260323_prof.jpg"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando título está em branco")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoTituloEmBranco() throws Exception {
        var dados = dadosPart("{\"titulo\":\"\",\"categoria\":\"tecnologia\",\"descricao\":\"Aprenda Java\",\"cargaHoraria\":\"40h\"}");

        mockMvc.perform(multipart("/cursos").part(dados))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando categoria está em branco")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoCategoriaEmBranco() throws Exception {
        var dados = dadosPart("{\"titulo\":\"Curso de Java\",\"categoria\":\"\",\"descricao\":\"Aprenda Java\",\"cargaHoraria\":\"40h\"}");

        mockMvc.perform(multipart("/cursos").part(dados))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando carga horária está em branco")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoCargaHorariaEmBranca() throws Exception {
        var dados = dadosPart("{\"titulo\":\"Curso de Java\",\"categoria\":\"tecnologia\",\"descricao\":\"Aprenda Java\",\"cargaHoraria\":\"\"}");

        mockMvc.perform(multipart("/cursos").part(dados))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 com cursos separados por ativos e arquivados")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200ComCursosListados() throws Exception {
        var curso = new CursoResponse(1L, "Curso de Java", "tecnologia", "Descrição", "40h", null, StatusCurso.PUBLICADO, 1L, LocalDateTime.now());
        when(cursoService.listar(any())).thenReturn(new ListaCursosResponse(List.of(curso), List.of()));

        mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.ativos").isArray())
                .andExpect(jsonPath("$.data.ativos.length()").value(1))
                .andExpect(jsonPath("$.data.arquivados").isArray())
                .andExpect(jsonPath("$.data.arquivados.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar 200 ao publicar curso (US-P13)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoPublicarCurso() throws Exception {
        doNothing().when(cursoService).alterarStatus(any(), any(), any());

        mockMvc.perform(patch("/cursos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLICADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 200 ao arquivar curso (US-P14)")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoArquivarCurso() throws Exception {
        doNothing().when(cursoService).alterarStatus(any(), any(), any());

        mockMvc.perform(patch("/cursos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARQUIVADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 422 para transição de status inválida")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar422ParaTransicaoInvalida() throws Exception {
        doThrow(new TransicaoStatusInvalidaException(StatusCurso.ARQUIVADO, StatusCurso.PUBLICADO))
                .when(cursoService).alterarStatus(any(), any(), any());

        mockMvc.perform(patch("/cursos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLICADO\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("Deve retornar 200 ao editar curso com sucesso")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoEditarCurso() throws Exception {
        when(cursoService.editar(any(), any(), any(), any())).thenReturn(new CursoResponse(
                1L, "Novo Título", "programação", "Nova descrição", "60h",
                null, StatusCurso.RASCUNHO, 1L, LocalDateTime.now()
        ));

        var dados = dadosPart("{\"titulo\":\"Novo Título\",\"categoria\":\"programação\",\"descricao\":\"Nova descrição\",\"cargaHoraria\":\"60h\"}");

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/cursos/1").part(dados))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.titulo").value("Novo Título"));
    }

    @Test
    @DisplayName("Deve retornar 404 ao editar curso inexistente ou de outro professor")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoEditarCursoInexistente() throws Exception {
        when(cursoService.editar(any(), any(), any(), any())).thenThrow(new CursoNaoEncontradoException(99L));

        var dados = dadosPart("{\"titulo\":\"T\",\"categoria\":\"c\",\"descricao\":\"d\",\"cargaHoraria\":\"10h\"}");

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/cursos/99").part(dados))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 200 com cursos encontrados na busca por texto")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200NaBuscaPorTexto() throws Exception {
        var curso = new CursoResponse(1L, "Curso de Java", "tecnologia", "Descrição", "40h", null, StatusCurso.PUBLICADO, 1L, LocalDateTime.now());
        when(cursoService.buscar(any(), any())).thenReturn(List.of(curso));

        mockMvc.perform(get("/cursos/buscar").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].titulo").value("Curso de Java"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando parâmetro q está em branco")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoTermoEmBranco() throws Exception {
        mockMvc.perform(get("/cursos/buscar").param("q", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 ao configurar dados de matrícula")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoConfigurarDadosDeMatricula() throws Exception {
        doNothing().when(cursoService).configurarMatricula(any(), any(), any());

        mockMvc.perform(patch("/cursos/1/dados-matricula")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "requerEndereco", true, "requerGenero", false, "requerIdade", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 404 ao configurar matrícula de curso inexistente")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar404AoConfigurarMatriculaDeCursoInexistente() throws Exception {
        doThrow(new CursoNaoEncontradoException(99L)).when(cursoService).configurarMatricula(any(), any(), any());

        mockMvc.perform(patch("/cursos/99/dados-matricula")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "requerEndereco", true, "requerGenero", false, "requerIdade", false
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private MockPart dadosPart(String json) {
        var part = new MockPart("dados", json.getBytes());
        part.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return part;
    }
}
