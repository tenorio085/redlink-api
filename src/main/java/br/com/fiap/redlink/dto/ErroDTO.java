package br.com.fiap.redlink.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * JSON padronizado de erro: todo erro da API sai neste formato.
 * A lista "campos" só aparece nos erros de validação (400).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroDTO(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        List<CampoInvalido> campos) {

    /** Um campo do JSON de entrada que não passou na validação. */
    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ErroDTO de(int status, String erro, String mensagem) {
        return new ErroDTO(status, erro, mensagem, agora(), null);
    }

    public static ErroDTO deValidacao(String mensagem, List<CampoInvalido> campos) {
        return new ErroDTO(400, "Dados inválidos", mensagem, agora(), campos);
    }

    private static LocalDateTime agora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
