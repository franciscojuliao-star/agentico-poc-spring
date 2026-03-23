package br.ufc.llm.curso.controller;

import br.ufc.llm.curso.dto.AlterarStatusRequest;
import br.ufc.llm.curso.dto.ConfigurarMatriculaRequest;
import br.ufc.llm.curso.dto.CriarCursoRequest;
import br.ufc.llm.curso.dto.CursoResponse;
import br.ufc.llm.curso.dto.EditarCursoRequest;
import br.ufc.llm.curso.dto.ListaCursosResponse;
import br.ufc.llm.curso.service.CursoService;
import br.ufc.llm.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "bearerAuth")
@Validated
@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    public ResponseEntity<ApiResponse<ListaCursosResponse>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(cursoService.listar(userDetails.getUsername()), "Cursos listados com sucesso."));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CursoResponse>> editar(
            @PathVariable Long id,
            @RequestPart("dados") @Valid EditarCursoRequest request,
            @RequestPart(value = "capa", required = false) MultipartFile capa,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(cursoService.editar(id, request, capa, userDetails.getUsername()), "Curso atualizado com sucesso."));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<java.util.List<CursoResponse>>> buscar(
            @RequestParam @NotBlank(message = "Termo de busca é obrigatório") String q,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(cursoService.buscar(q, userDetails.getUsername()), "Busca realizada com sucesso."));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> alterarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AlterarStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        cursoService.alterarStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Status do curso atualizado com sucesso."));
    }

    @PatchMapping("/{id}/dados-matricula")
    public ResponseEntity<ApiResponse<Void>> configurarMatricula(
            @PathVariable Long id,
            @RequestBody ConfigurarMatriculaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        cursoService.configurarMatricula(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Dados de matrícula configurados com sucesso."));
    }

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
