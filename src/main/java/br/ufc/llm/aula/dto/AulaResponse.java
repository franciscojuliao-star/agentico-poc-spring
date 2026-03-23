package br.ufc.llm.aula.dto;

import br.ufc.llm.aula.domain.TipoArquivo;

public record AulaResponse(
        Long id,
        String nome,
        int ordem,
        String arquivo,
        TipoArquivo tipoArquivo,
        String conteudoCkEditor,
        String conteudoGerado,
        Long moduloId
) {}
