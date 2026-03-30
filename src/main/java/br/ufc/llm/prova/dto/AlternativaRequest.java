package br.ufc.llm.prova.dto;

import jakarta.validation.constraints.NotBlank;

public record AlternativaRequest(
        @NotBlank(message = "Texto da alternativa é obrigatório")
        String texto,
        boolean correta
) {}
