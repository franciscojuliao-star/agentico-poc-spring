package br.ufc.llm.prova.dto;

import java.util.List;

public record ProvaResponse(
        Long id,
        Long moduloId,
        boolean mostrarRespostasErradas,
        boolean mostrarRespostasCorretas,
        boolean mostrarValores,
        List<PerguntaResponse> perguntas
) {}
