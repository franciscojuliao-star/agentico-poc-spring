package br.ufc.llm.admin.controller;

import br.ufc.llm.admin.service.AdminService;
import br.ufc.llm.shared.dto.ApiResponse;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuarios() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listarUsuarios(), "Usuários listados com sucesso."));
    }

    @PatchMapping("/usuarios/{id}/ativar")
    public ResponseEntity<ApiResponse<Void>> ativar(@PathVariable Long id) {
        adminService.ativar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Conta ativada com sucesso."));
    }

    @PatchMapping("/usuarios/{id}/desativar")
    public ResponseEntity<ApiResponse<Void>> desativar(@PathVariable Long id) {
        adminService.desativar(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Conta desativada com sucesso."));
    }
}
