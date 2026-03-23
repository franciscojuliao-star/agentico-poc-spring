package br.ufc.llm.prova.dto;

public record AlternativaResponse(
        Long id,
        String texto,
        boolean correta
) {}
