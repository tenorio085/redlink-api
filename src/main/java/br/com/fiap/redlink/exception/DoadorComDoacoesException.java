package br.com.fiap.redlink.exception;

/** Tentativa de remover um doador que já tem doações registradas. */
public class DoadorComDoacoesException extends RegraNegocioException {

    public DoadorComDoacoesException(long quantidade) {
        super("Doador possui doações",
                "Este doador tem " + quantidade + " doação(ões) registrada(s) e não pode ser removido. "
                        + "O histórico de doações precisa ser preservado.");
    }
}
