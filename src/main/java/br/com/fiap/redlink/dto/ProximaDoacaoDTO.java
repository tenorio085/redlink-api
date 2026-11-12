package br.com.fiap.redlink.dto;

import java.time.LocalDate;

/**
 * Resposta do "Posso doar?": diz se o doador já está liberado e a partir de que dia.
 */
public record ProximaDoacaoDTO(
        Long doadorId,
        boolean podeDoar,
        LocalDate proximaDataPermitida,
        int intervaloMinimoDias,
        String mensagem) {
}
