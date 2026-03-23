package br.ufc.llm.modulo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReordenarModuloRequest(
        @NotNull(message = "Nova ordem é obrigatória")
        @Min(value = 1, message = "Ordem deve ser maior que zero")
        Integer novaOrdem
) {}
