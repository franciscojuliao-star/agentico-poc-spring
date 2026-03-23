package br.ufc.llm.aula.exception;

public class ConteudoGeradoAusenteException extends RuntimeException {

    public ConteudoGeradoAusenteException() {
        super("Não há conteúdo gerado para confirmar. Gere o conteúdo antes de confirmar.");
    }
}
