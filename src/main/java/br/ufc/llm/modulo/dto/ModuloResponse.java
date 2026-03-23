package br.ufc.llm.modulo.dto;

public record ModuloResponse(
        Long id,
        String nome,
        int ordem,
        String capa,
        Long cursoId
) {}
