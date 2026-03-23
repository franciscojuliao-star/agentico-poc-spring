package br.ufc.llm.perfil.controller;

import br.ufc.llm.perfil.dto.PerfilResponse;
import br.ufc.llm.perfil.exception.SenhaAtualInvalidaException;
import br.ufc.llm.perfil.exception.TipoArquivoInvalidoException;
import br.ufc.llm.perfil.service.PerfilService;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.ufc.llm.shared.security.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpMethod;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerfilController.class)
@AutoConfigureMockMvc(addFilters = false)
class PerfilControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PerfilService perfilService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 ao fazer upload de foto válida")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoFazerUploadDeFoto() throws Exception {
        doNothing().when(perfilService).atualizarFoto(any(), any());

        var foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(HttpMethod.PATCH, "/perfil/foto").file(foto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 400 quando arquivo não é imagem")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoArquivoNaoEImagem() throws Exception {
        doThrow(new TipoArquivoInvalidoException()).when(perfilService).atualizarFoto(any(), any());

        var arquivo = new MockMultipartFile("foto", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart(HttpMethod.PATCH, "/perfil/foto").file(arquivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar 400 quando arquivo enviado está vazio")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoNenhumArquivoEnviado() throws Exception {
        doThrow(new TipoArquivoInvalidoException()).when(perfilService).atualizarFoto(any(), any());

        var arquivoVazio = new MockMultipartFile("foto", new byte[0]);

        mockMvc.perform(multipart(HttpMethod.PATCH, "/perfil/foto").file(arquivoVazio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar 200 com dados do perfil e CPF mascarado")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200ComDadosDoPerfil() throws Exception {
        when(perfilService.buscarPerfil("prof@email.com")).thenReturn(new PerfilResponse(
                1L, "Professor Silva", "***.***.***-01", "prof@email.com",
                PerfilUsuario.PROFESSOR, StatusUsuario.ATIVO, null, LocalDateTime.now()
        ));

        mockMvc.perform(get("/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.nome").value("Professor Silva"))
                .andExpect(jsonPath("$.data.cpf").value("***.***.***-01"))
                .andExpect(jsonPath("$.data.email").value("prof@email.com"));
    }

    @Test
    @DisplayName("Deve retornar 200 ao alterar senha com sucesso")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar200AoAlterarSenha() throws Exception {
        doNothing().when(perfilService).alterarSenha(any(), any(), any());

        mockMvc.perform(patch("/perfil/senha")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "senhaAtual", "senhaAtual123",
                                "novaSenha", "novaSenha123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 400 quando senha atual está incorreta")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoSenhaAtualIncorreta() throws Exception {
        doThrow(new SenhaAtualInvalidaException()).when(perfilService).alterarSenha(any(), any(), any());

        mockMvc.perform(patch("/perfil/senha")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "senhaAtual", "errada",
                                "novaSenha", "novaSenha123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve retornar 400 quando nova senha tem menos de 8 caracteres")
    @WithMockUser(username = "prof@email.com")
    void deveRetornar400QuandoNovaSenhaCurta() throws Exception {
        mockMvc.perform(patch("/perfil/senha")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "senhaAtual", "senhaAtual123",
                                "novaSenha", "curta"
                        ))))
                .andExpect(status().isBadRequest());
    }
}
