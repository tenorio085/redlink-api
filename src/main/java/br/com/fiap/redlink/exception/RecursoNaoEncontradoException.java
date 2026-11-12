package br.com.fiap.redlink.exception;

/** Doador, doação ou hemocentro que não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não foi encontrado(a).");
    }
}
