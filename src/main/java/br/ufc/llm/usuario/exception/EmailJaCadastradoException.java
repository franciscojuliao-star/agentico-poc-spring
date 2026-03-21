package br.ufc.llm.usuario.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("E-mail já cadastrado: " + email);
    }
}
