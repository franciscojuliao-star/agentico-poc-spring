package br.ufc.llm.admin.controller;

import br.ufc.llm.admin.service.AdminService;
import br.ufc.llm.shared.dto.ApiResponse;
import br.ufc.llm.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import java.util.List;

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
}
