package br.com.fiap.redlink.exception;

/** Mais de 4 doações (homens) ou 3 (mulheres) em 12 meses. */
public class LimiteAnualException extends RegraNegocioException {

    public LimiteAnualException(String mensagem) {
        super("Limite anual de doações atingido", mensagem);
    }
}
