package br.ufc.llm.prova.controller;

import br.ufc.llm.prova.dto.*;
import br.ufc.llm.prova.service.ProvaService;
import br.ufc.llm.prova.service.QuizIaService;
import br.ufc.llm.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class ProvaController {

    private final ProvaService provaService;
    private final QuizIaService quizIaService;

    // ── Prova ────────────────────────────────────────────

    @PostMapping("/modulos/{moduloId}/prova")
    public ResponseEntity<ApiResponse<ProvaResponse>> criar(
            @PathVariable Long moduloId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = provaService.criar(moduloId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Prova criada com sucesso."));
    }

    @GetMapping("/modulos/{moduloId}/prova")
    public ResponseEntity<ApiResponse<ProvaResponse>> buscar(
            @PathVariable Long moduloId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(provaService.buscar(moduloId, userDetails.getUsername()), "Prova encontrada."));
    }

    @PutMapping("/provas/{id}")
    public ResponseEntity<ApiResponse<ProvaResponse>> configurar(
            @PathVariable Long id,
            @RequestBody ConfigurarProvaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(provaService.configurar(id, request, userDetails.getUsername()), "Prova configurada com sucesso."));
    }

    @DeleteMapping("/provas/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        provaService.excluir(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Perguntas ─────────────────────────────────────────

    @PostMapping("/provas/{provaId}/perguntas")
    public ResponseEntity<ApiResponse<PerguntaResponse>> adicionarPergunta(
            @PathVariable Long provaId,
            @RequestBody @Valid PerguntaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = provaService.adicionarPergunta(provaId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Pergunta adicionada com sucesso."));
    }

    @PutMapping("/perguntas/{id}")
    public ResponseEntity<ApiResponse<PerguntaResponse>> editarPergunta(
            @PathVariable Long id,
            @RequestBody @Valid PerguntaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(provaService.editarPergunta(id, request, userDetails.getUsername()), "Pergunta atualizada."));
    }

    @DeleteMapping("/perguntas/{id}")
    public ResponseEntity<Void> excluirPergunta(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        provaService.excluirPergunta(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Alternativas ──────────────────────────────────────

    @PostMapping("/perguntas/{perguntaId}/alternativas")
    public ResponseEntity<ApiResponse<AlternativaResponse>> adicionarAlternativa(
            @PathVariable Long perguntaId,
            @RequestBody @Valid AlternativaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = provaService.adicionarAlternativa(perguntaId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Alternativa adicionada."));
    }

    @PutMapping("/alternativas/{id}")
    public ResponseEntity<ApiResponse<AlternativaResponse>> editarAlternativa(
            @PathVariable Long id,
            @RequestBody @Valid AlternativaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(provaService.editarAlternativa(id, request, userDetails.getUsername()), "Alternativa atualizada."));
    }

    @DeleteMapping("/alternativas/{id}")
    public ResponseEntity<Void> excluirAlternativa(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        provaService.excluirAlternativa(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ── Quiz via IA ───────────────────────────────────────

    @PostMapping("/modulos/{moduloId}/prova/gerar-quiz-ia")
    public ResponseEntity<ApiResponse<QuizGeradoResponse>> gerarQuizIa(
            @PathVariable Long moduloId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = quizIaService.gerarQuiz(moduloId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response, "Quiz gerado. Revise e salve as perguntas."));
    }

    @PostMapping("/modulos/{moduloId}/prova/salvar-quiz")
    public ResponseEntity<ApiResponse<Void>> salvarQuiz(
            @PathVariable Long moduloId,
            @RequestBody List<@Valid PerguntaRequest> perguntas,
            @AuthenticationPrincipal UserDetails userDetails) {
        quizIaService.salvarPerguntas(moduloId, perguntas, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null, "Perguntas salvas na prova com sucesso."));
    }
}
