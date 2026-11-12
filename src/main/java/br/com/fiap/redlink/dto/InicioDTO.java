package br.com.fiap.redlink.dto;

import java.util.List;

/**
 * Resposta da rota inicial (GET /): mostra que a API está no ar e quais rotas existem.
 */
public record InicioDTO(
        String api,
        String status,
        String descricao,
        List<String> rotas) {
}
