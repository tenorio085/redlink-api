package br.com.fiap.redlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados que a API recebe para cadastrar ou atualizar um hemocentro.
 */
public class HemocentroRequestDTO {

    @NotBlank(message = "O nome do hemocentro é obrigatório.")
    @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "O endereço é obrigatório.")
    @Size(max = 200, message = "O endereço pode ter no máximo 200 caracteres.")
    private String endereco;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 80, message = "A cidade pode ter no máximo 80 caracteres.")
    private String cidade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
