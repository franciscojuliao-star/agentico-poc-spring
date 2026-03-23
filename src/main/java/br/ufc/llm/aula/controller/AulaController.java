package br.ufc.llm.aula.controller;

import br.ufc.llm.aula.dto.AulaResponse;
import br.ufc.llm.aula.dto.CriarAulaRequest;
import br.ufc.llm.aula.dto.EditarAulaRequest;
import br.ufc.llm.aula.dto.ReordenarAulaRequest;
import br.ufc.llm.aula.service.AulaService;
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
public class AulaController {

    private final AulaService aulaService;

    @PostMapping(value = "/modulos/{moduloId}/aulas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AulaResponse>> adicionar(
            @PathVariable Long moduloId,
            @RequestPart("dados") @Valid CriarAulaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = aulaService.adicionar(moduloId, request, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Aula adicionada com sucesso."));
    }

    @PatchMapping(value = "/aulas/{id}/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AulaResponse>> atualizarArquivo(
            @PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = aulaService.atualizarArquivo(id, arquivo, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Arquivo da aula atualizado com sucesso."));
    }

    @PutMapping("/aulas/{id}")
    public ResponseEntity<ApiResponse<AulaResponse>> editar(
            @PathVariable Long id,
            @RequestBody @Valid EditarAulaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = aulaService.editar(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Aula atualizada com sucesso."));
    }

    @DeleteMapping("/aulas/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        aulaService.excluir(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/aulas/{id}/ordem")
    public ResponseEntity<ApiResponse<Void>> reordenar(
            @PathVariable Long id,
            @RequestBody @Valid ReordenarAulaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        aulaService.reordenar(id, request.novaOrdem(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Aula reordenada com sucesso."));
    }
}
