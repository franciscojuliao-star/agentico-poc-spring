package br.ufc.llm.prova.dto;

import java.util.List;

public record PerguntaResponse(
        Long id,
        String enunciado,
        int pontos,
        int ordem,
        List<AlternativaResponse> alternativas
) {}
