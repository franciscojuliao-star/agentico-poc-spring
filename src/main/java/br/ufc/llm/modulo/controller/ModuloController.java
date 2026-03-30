package br.ufc.llm.modulo.controller;

import br.ufc.llm.modulo.dto.ModuloResponse;
import br.ufc.llm.modulo.dto.ReordenarModuloRequest;
import br.ufc.llm.modulo.service.ModuloService;
import br.ufc.llm.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping("/cursos/{cursoId}/modulos")
    public ResponseEntity<ApiResponse<java.util.List<ModuloResponse>>> listar(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(moduloService.listar(cursoId, userDetails.getUsername()), "Módulos listados com sucesso."));
    }

    @PostMapping("/cursos/{cursoId}/modulos")
    public ResponseEntity<ApiResponse<ModuloResponse>> adicionar(
            @PathVariable Long cursoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = moduloService.adicionar(cursoId, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Módulo adicionado com sucesso."));
    }

    @PatchMapping(value = "/modulos/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ModuloResponse>> atualizarCapa(
            @PathVariable Long id,
            @RequestPart("capa") MultipartFile capa,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = moduloService.atualizarCapa(id, capa, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Capa do módulo atualizada com sucesso."));
    }

    @DeleteMapping("/modulos/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        moduloService.excluir(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/modulos/{id}/ordem")
    public ResponseEntity<ApiResponse<Void>> reordenar(
            @PathVariable Long id,
            @RequestBody @Valid ReordenarModuloRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        moduloService.reordenar(id, request.novaOrdem(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Módulo reordenado com sucesso."));
    }
}
