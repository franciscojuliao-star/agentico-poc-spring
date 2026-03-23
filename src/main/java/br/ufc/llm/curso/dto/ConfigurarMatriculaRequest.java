package br.ufc.llm.curso.dto;

public record ConfigurarMatriculaRequest(
        boolean requerEndereco,
        boolean requerGenero,
        boolean requerIdade
) {}
