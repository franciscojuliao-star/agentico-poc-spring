package br.ufc.llm.curso.dto;

import br.ufc.llm.curso.domain.StatusCurso;

import java.time.LocalDateTime;

public record CursoResponse(
        Long id,
        String titulo,
        String categoria,
        String descricao,
        String cargaHoraria,
        String capa,
        StatusCurso status,
        Long professorId,
        LocalDateTime criadoEm
) {}
