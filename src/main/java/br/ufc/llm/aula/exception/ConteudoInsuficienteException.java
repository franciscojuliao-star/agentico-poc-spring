package br.ufc.llm.aula.exception;

public class ConteudoInsuficienteException extends RuntimeException {

    public ConteudoInsuficienteException() {
        super("A aula não possui arquivo PDF nem conteúdo CKEditor para gerar conteúdo via IA.");
    }
}
