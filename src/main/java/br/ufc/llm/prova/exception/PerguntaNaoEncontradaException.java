package br.ufc.llm.prova.exception;

public class PerguntaNaoEncontradaException extends RuntimeException {

    public PerguntaNaoEncontradaException(Long id) {
        super("Pergunta não encontrada: " + id);
    }
}
