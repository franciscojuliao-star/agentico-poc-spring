package br.ufc.llm.aula.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarAulaRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome
) {}
