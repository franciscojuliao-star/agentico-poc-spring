package br.ufc.llm.perfil.exception;

public class SenhaAtualInvalidaException extends RuntimeException {

    public SenhaAtualInvalidaException() {
        super("Senha atual incorreta.");
    }
}
