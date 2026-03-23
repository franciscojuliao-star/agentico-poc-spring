package br.ufc.llm.admin.controller;

import br.ufc.llm.admin.service.AdminService;
import br.ufc.llm.shared.security.JwtAuthFilter;
import br.ufc.llm.usuario.domain.PerfilUsuario;
import br.ufc.llm.usuario.domain.StatusUsuario;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import br.ufc.llm.usuario.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AdminService adminService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("Deve retornar 200 com lista de usuários")
    void deveRetornar200ComListaDeUsuarios() throws Exception {
        when(adminService.listarUsuarios()).thenReturn(List.of(
                new UsuarioResponse(1L, "Professor Silva", "000.000.000-00", "prof@email.com",
                        PerfilUsuario.PROFESSOR, StatusUsuario.INATIVO, LocalDateTime.now()),
                new UsuarioResponse(2L, "Administrador", "111.111.111-11", "admin@ufc.br",
                        PerfilUsuario.ADMIN, StatusUsuario.ATIVO, LocalDateTime.now())
        ));

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].nome").value("Professor Silva"))
                .andExpect(jsonPath("$.data[1].nome").value("Administrador"));
    }

    @Test
    @DisplayName("Deve retornar 200 com lista vazia quando não há usuários")
    void deveRetornar200ComListaVazia() throws Exception {
        when(adminService.listarUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar 200 ao ativar conta com sucesso")
    void deveRetornar200AoAtivarConta() throws Exception {
        doNothing().when(adminService).ativar(1L);

        mockMvc.perform(patch("/admin/usuarios/1/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 404 ao ativar conta com id inexistente")
    void deveRetornar404AoAtivarContaInexistente() throws Exception {
        doThrow(new UsuarioNaoEncontradoException(99L)).when(adminService).ativar(99L);

        mockMvc.perform(patch("/admin/usuarios/99/ativar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve retornar 200 ao desativar conta com sucesso")
    void deveRetornar200AoDesativarConta() throws Exception {
        doNothing().when(adminService).desativar(1L);

        mockMvc.perform(patch("/admin/usuarios/1/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("Deve retornar 404 ao desativar conta com id inexistente")
    void deveRetornar404AoDesativarContaInexistente() throws Exception {
        doThrow(new UsuarioNaoEncontradoException(99L)).when(adminService).desativar(99L);

        mockMvc.perform(patch("/admin/usuarios/99/desativar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
