package br.ufc.llm.perfil.controller;

import br.ufc.llm.perfil.dto.PerfilResponse;
import br.ufc.llm.perfil.service.PerfilService;
import br.ufc.llm.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    public ResponseEntity<ApiResponse<PerfilResponse>> buscarPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(perfilService.buscarPerfil(userDetails.getUsername()), "Perfil carregado com sucesso."));
    }

    @PatchMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> atualizarFoto(
            @RequestParam("foto") MultipartFile foto,
            @AuthenticationPrincipal UserDetails userDetails) {
        perfilService.atualizarFoto(foto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Foto de perfil atualizada com sucesso."));
    }
}
