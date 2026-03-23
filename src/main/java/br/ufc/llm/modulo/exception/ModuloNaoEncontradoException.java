package br.ufc.llm.modulo.exception;

public class ModuloNaoEncontradoException extends RuntimeException {

    public ModuloNaoEncontradoException(Long id) {
        super("Módulo não encontrado: " + id);
    }
}
