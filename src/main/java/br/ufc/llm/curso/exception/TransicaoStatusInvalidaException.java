package br.ufc.llm.curso.exception;

import br.ufc.llm.curso.domain.StatusCurso;

public class TransicaoStatusInvalidaException extends RuntimeException {

    public TransicaoStatusInvalidaException(StatusCurso atual, StatusCurso novo) {
        super("Transição de status inválida: " + atual + " → " + novo);
    }
}
