package br.ufc.llm.curso.dto;

import java.util.List;

public record ListaCursosResponse(
        List<CursoResponse> ativos,
        List<CursoResponse> arquivados
) {}
