package br.com.fiap.redlink.model;

import java.time.LocalDateTime;

/**
 * Uma doação agendada ou já realizada (tabela tb_rl_doacao).
 * É o lado "N" do relacionamento 1:N com {@link Doador}.
 */
public class Doacao {

    private Long id;
    private LocalDateTime dataDoacao;
    private StatusDoacao status;
    private Doador doador;
    private Hemocentro hemocentro;

    public Doacao() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDateTime dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public Doador getDoador() {
        return doador;
    }

    public void setDoador(Doador doador) {
        this.doador = doador;
    }

    public Hemocentro getHemocentro() {
        return hemocentro;
    }

    public void setHemocentro(Hemocentro hemocentro) {
        this.hemocentro = hemocentro;
    }
}
