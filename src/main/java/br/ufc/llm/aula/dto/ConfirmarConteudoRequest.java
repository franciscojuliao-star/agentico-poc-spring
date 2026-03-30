package br.ufc.llm.aula.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarConteudoRequest(

        @NotBlank(message = "Conteúdo confirmado é obrigatório")
        String conteudo
) {}
