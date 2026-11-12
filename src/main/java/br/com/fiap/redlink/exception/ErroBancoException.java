package br.com.fiap.redlink.exception;

/**
 * Embrulha a SQLException lançada pelo JDBC. O detalhe do SQL fica só no log
 * do servidor: o handler devolve HTTP 500 com uma mensagem genérica.
 */
public class ErroBancoException extends RuntimeException {

    public ErroBancoException(String operacao, Throwable causa) {
        super("Falha no banco de dados ao " + operacao, causa);
    }
}
