package br.ufc.llm.prova.dto;

public record ConfigurarProvaRequest(
        boolean mostrarRespostasErradas,
        boolean mostrarRespostasCorretas,
        boolean mostrarValores
) {}
