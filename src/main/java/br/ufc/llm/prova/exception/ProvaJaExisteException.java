package br.ufc.llm.prova.exception;

public class ProvaJaExisteException extends RuntimeException {

    public ProvaJaExisteException(Long moduloId) {
        super("Já existe uma prova para o módulo: " + moduloId);
    }
}
