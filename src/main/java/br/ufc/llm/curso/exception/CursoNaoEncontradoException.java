package br.ufc.llm.curso.exception;

public class CursoNaoEncontradoException extends RuntimeException {

    public CursoNaoEncontradoException(Long id) {
        super("Curso não encontrado: " + id);
    }
}
