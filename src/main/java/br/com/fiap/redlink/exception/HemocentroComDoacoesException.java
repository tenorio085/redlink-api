package br.com.fiap.redlink.exception;

/** Tentativa de remover um hemocentro que já recebeu doações. */
public class HemocentroComDoacoesException extends RegraNegocioException {

    public HemocentroComDoacoesException(long quantidade) {
        super("Hemocentro possui doações",
                "Este hemocentro tem " + quantidade + " doação(ões) registrada(s) e não pode ser removido.");
    }
}
