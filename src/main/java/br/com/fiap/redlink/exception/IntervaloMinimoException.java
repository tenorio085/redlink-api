package br.com.fiap.redlink.exception;

/** Doação antes de 60 dias (homens) ou 90 dias (mulheres) da anterior. */
public class IntervaloMinimoException extends RegraNegocioException {

    public IntervaloMinimoException(String mensagem) {
        super("Intervalo mínimo não cumprido", mensagem);
    }
}
