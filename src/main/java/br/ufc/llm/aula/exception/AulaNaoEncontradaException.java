package br.ufc.llm.aula.exception;

public class AulaNaoEncontradaException extends RuntimeException {

    public AulaNaoEncontradaException(Long id) {
        super("Aula não encontrada: " + id);
    }
}
