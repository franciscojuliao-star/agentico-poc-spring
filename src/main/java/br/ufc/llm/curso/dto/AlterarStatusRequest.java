package br.ufc.llm.curso.dto;

import br.ufc.llm.curso.domain.StatusCurso;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(

        @NotNull(message = "Status é obrigatório")
        StatusCurso status
) {}
