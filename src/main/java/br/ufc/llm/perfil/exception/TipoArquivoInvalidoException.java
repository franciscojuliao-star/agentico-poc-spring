package br.ufc.llm.perfil.exception;

public class TipoArquivoInvalidoException extends RuntimeException {

    public TipoArquivoInvalidoException() {
        super("Apenas imagens são permitidas (JPEG, PNG, GIF, WEBP).");
    }
}
