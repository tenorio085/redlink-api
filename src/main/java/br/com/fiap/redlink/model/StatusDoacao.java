package br.com.fiap.redlink.model;

/**
 * Situações possíveis de uma doação. Os nomes são os mesmos aceitos
 * pelo CHECK da coluna ds_status no banco.
 */
public enum StatusDoacao {

    AGENDADA,
    REALIZADA,
    NAO_COMPARECEU,
    CANCELADA;

    /**
     * Doações agendadas ou realizadas "ocupam" o calendário do doador:
     * são elas que contam para o intervalo mínimo e para o limite anual.
     */
    public boolean contaParaAsRegras() {
        return this == AGENDADA || this == REALIZADA;
    }
}
