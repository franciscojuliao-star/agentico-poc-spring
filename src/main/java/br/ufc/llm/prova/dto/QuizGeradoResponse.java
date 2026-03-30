package br.ufc.llm.prova.dto;

import java.util.List;

public record QuizGeradoResponse(
        List<PerguntaResponse> perguntas
) {}
