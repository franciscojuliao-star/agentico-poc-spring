package br.ufc.llm.aula.controller;

import br.ufc.llm.aula.dto.ConteudoGeradoResponse;
import br.ufc.llm.aula.service.AulaIaService;
import br.ufc.llm.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/aulas")
@RequiredArgsConstructor
public class AulaIaController {

    private final AulaIaService aulaIaService;

    @PostMapping("/{id}/gerar-conteudo")
    public ResponseEntity<ApiResponse<ConteudoGeradoResponse>> gerarConteudo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = aulaIaService.gerarConteudo(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Conteúdo gerado com sucesso. Revise e confirme."));
    }

    @PostMapping("/{id}/confirmar-conteudo")
    public ResponseEntity<ApiResponse<ConteudoGeradoResponse>> confirmarConteudo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = aulaIaService.confirmarConteudo(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Conteúdo confirmado com sucesso."));
    }
}
