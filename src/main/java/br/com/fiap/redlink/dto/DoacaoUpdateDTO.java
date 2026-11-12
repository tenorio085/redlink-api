package br.com.fiap.redlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * Dados que a API recebe para ATUALIZAR uma doação (PUT /doacoes/{id}).
 * É aqui que o hemocentro marca REALIZADA, NAO_COMPARECEU ou CANCELADA.
 * A data não leva @Future porque, no dia da doação, ela já não está mais no futuro.
 */
public class DoacaoUpdateDTO {

    @NotNull(message = "A data e o horário da doação são obrigatórios.")
    private LocalDateTime dataDoacao;

    @NotNull(message = "O id do hemocentro é obrigatório.")
    @Positive(message = "O id do hemocentro deve ser um número positivo.")
    private Long hemocentroId;

    @NotBlank(message = "O status é obrigatório.")
    @Pattern(regexp = "AGENDADA|REALIZADA|NAO_COMPARECEU|CANCELADA",
            message = "Status inválido. Use AGENDADA, REALIZADA, NAO_COMPARECEU ou CANCELADA.")
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
