package br.ufc.llm.auth.exception;

public class TokenInvalidoOuExpiradoException extends RuntimeException {

    public TokenInvalidoOuExpiradoException() {
        super("Token inválido ou expirado.");
    }
}
