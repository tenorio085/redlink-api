package br.com.fiap.redlink.dto;

import java.time.LocalDateTime;

/**
 * Dados da doação que a API devolve. Em vez dos objetos completos,
 * traz só o id e o nome do doador e do hemocentro.
 */
public class DoacaoResponseDTO {

    private Long id;
    private LocalDateTime dataDoacao;
    private String status;
    private Long doadorId;
    private String doadorNome;
    private Long hemocentroId;
    private String hemocentroNome;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDoadorId() {
        return doadorId;
    }

    public void setDoadorId(Long doadorId) {
        this.doadorId = doadorId;
    }

    public String getDoadorNome() {
        return doadorNome;
    }

    public void setDoadorNome(String doadorNome) {
        this.doadorNome = doadorNome;
    }

    public Long getHemocentroId() {
        return hemocentroId;
    }

    public void setHemocentroId(Long hemocentroId) {
        this.hemocentroId = hemocentroId;
    }

    public String getHemocentroNome() {
        return hemocentroNome;
    }

    public void setHemocentroNome(String hemocentroNome) {
        this.hemocentroNome = hemocentroNome;
    }
}
