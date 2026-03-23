package br.ufc.llm.aula.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReordenarAulaRequest(
        @NotNull(message = "Nova ordem é obrigatória")
        @Min(value = 1, message = "Ordem deve ser maior que zero")
        Integer novaOrdem
) {}
