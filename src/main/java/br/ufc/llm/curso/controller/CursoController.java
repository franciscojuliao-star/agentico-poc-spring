package br.ufc.llm.curso.controller;

import br.ufc.llm.curso.dto.CriarCursoRequest;
import br.ufc.llm.curso.dto.CursoResponse;
import br.ufc.llm.curso.service.CursoService;
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
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CursoResponse>> criar(
            @RequestPart("dados") @Valid CriarCursoRequest request,
            @RequestPart(value = "capa", required = false) MultipartFile capa,
            @AuthenticationPrincipal UserDetails userDetails) {
        CursoResponse response = cursoService.criar(request, capa, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Curso criado com sucesso."));
    }
}
