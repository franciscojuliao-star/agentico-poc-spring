package br.ufc.llm.aula.dto;

import jakarta.validation.constraints.NotBlank;

public record EditarAulaRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        String conteudoCkEditor
) {}
