package br.com.fiap.redlink.exception;

/** Doador com idade fora da faixa de 16 a 69 anos. */
public class IdadeNaoPermitidaException extends RegraNegocioException {

    public IdadeNaoPermitidaException(String mensagem) {
        super("Idade não permitida para doação", mensagem);
    }
}
