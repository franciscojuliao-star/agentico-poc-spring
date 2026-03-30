package br.ufc.llm.prova.exception;

public class ProvaNaoEncontradaException extends RuntimeException {

    public ProvaNaoEncontradaException(Long moduloId) {
        super("Prova não encontrada para o módulo: " + moduloId);
    }
}
