package br.com.fiap.redlink.controller;

import br.com.fiap.redlink.dto.InicioDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rota inicial: quem abre o endereço da API no navegador vê que ela está no ar
 * e quais são as rotas disponíveis. Não acessa o banco.
 */
@RestController
public class InicioController {

    @GetMapping("/")
    public InicioDTO inicio() {
        return new InicioDTO(
                "RedLink API",
                "online",
                "O sangue certo, do doador certo, na hora certa.",
                List.of(
                        "GET /doadores",
                        "GET /doadores/{id}",
                        "GET /doadores/{id}/doacoes",
                        "GET /doadores/{id}/proxima-doacao",
                        "GET /doacoes",
                        "GET /doacoes/{id}",
                        "GET /hemocentros",
                        "GET /hemocentros/{id}"));
    }
}
