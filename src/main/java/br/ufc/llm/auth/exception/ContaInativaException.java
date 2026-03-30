package br.ufc.llm.auth.exception;

public class ContaInativaException extends RuntimeException {

    public ContaInativaException() {
        super("Conta inativa. Aguarde a ativação pelo administrador.");
    }
}
