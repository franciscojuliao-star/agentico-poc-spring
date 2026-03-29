package br.ufc.llm.prova.exception;

public class RespostaIaMalformadaException extends RuntimeException {

    public RespostaIaMalformadaException() {
        super("A IA retornou uma resposta em formato inválido. Tente novamente.");
    }
}
