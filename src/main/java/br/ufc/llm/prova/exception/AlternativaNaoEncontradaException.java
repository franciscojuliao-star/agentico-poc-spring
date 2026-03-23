package br.ufc.llm.prova.exception;

public class AlternativaNaoEncontradaException extends RuntimeException {

    public AlternativaNaoEncontradaException(Long id) {
        super("Alternativa não encontrada: " + id);
    }
}
