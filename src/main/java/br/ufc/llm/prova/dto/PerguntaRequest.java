package br.ufc.llm.prova.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PerguntaRequest(
        @NotBlank(message = "Enunciado é obrigatório")
        String enunciado,

        @Min(value = 1, message = "Pontos devem ser no mínimo 1")
        int pontos,

        @NotEmpty(message = "A pergunta deve ter ao menos uma alternativa")
        @Valid
        List<AlternativaRequest> alternativas
) {}
