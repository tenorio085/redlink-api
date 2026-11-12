package br.com.fiap.redlink.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Dados que a API recebe para AGENDAR uma doação (POST /doadores/{id}/doacoes).
 * O doador vem da rota e o status inicial é sempre AGENDADA.
 */
public class DoacaoRequestDTO {

    @NotNull(message = "A data e o horário da doação são obrigatórios.")
    @Future(message = "A doação deve ser agendada para uma data futura.")
    private LocalDateTime dataDoacao;

    @NotNull(message = "O id do hemocentro é obrigatório.")
    @Positive(message = "O id do hemocentro deve ser um número positivo.")
    private Long hemocentroId;

    public LocalDateTime getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDateTime dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public Long getHemocentroId() {
        return hemocentroId;
    }

    public void setHemocentroId(Long hemocentroId) {
        this.hemocentroId = hemocentroId;
    }
}
