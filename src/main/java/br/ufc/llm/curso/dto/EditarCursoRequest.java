package br.ufc.llm.curso.dto;

import jakarta.validation.constraints.NotBlank;

public record EditarCursoRequest(

        @NotBlank(message = "Título é obrigatório")
        String titulo,

        @NotBlank(message = "Categoria é obrigatória")
        String categoria,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotBlank(message = "Carga horária é obrigatória")
        String cargaHoraria
) {}
