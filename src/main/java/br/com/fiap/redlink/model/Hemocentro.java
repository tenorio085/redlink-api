package br.com.fiap.redlink.model;

/**
 * Local onde as doações acontecem (tabela tb_rl_hemocentro).
 */
public class Hemocentro {

    private Long id;
    private String nome;
    private String endereco;
    private String cidade;

    public Hemocentro() {
    }

    public Hemocentro(Long id, String nome, String endereco, String cidade) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.cidade = cidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
