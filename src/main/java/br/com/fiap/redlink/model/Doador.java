package br.com.fiap.redlink.model;

import br.com.fiap.redlink.exception.IdadeNaoPermitidaException;
import br.com.fiap.redlink.exception.IntervaloMinimoException;
import br.com.fiap.redlink.exception.LimiteAnualException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Pessoa que doa sangue (tabela tb_rl_doador).
 * <p>
 * As regras oficiais de doação ficam aqui, dentro do domínio:
 * idade de 16 a 69 anos, intervalo de 60 dias (homens) ou 90 dias (mulheres)
 * entre doações e limite de 4 (homens) ou 3 (mulheres) doações em 12 meses.
 * Fonte: Secretaria da Saúde do Estado de São Paulo.
 */
public class Doador {

    public static final int IDADE_MINIMA = 16;
    public static final int IDADE_MAXIMA = 69;

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Long id;
    private String nome;
    private String cpf;
    private String sexo;
    private LocalDate dataNascimento;
    private BigDecimal peso;
    private String email;
    private String telefone;
    private String tipoSanguineo;

    public Doador() {
    }

    // ------------------------------------------------------------------
    // Regras de negócio
    // ------------------------------------------------------------------

    private boolean homem() {
        return "M".equals(sexo);
    }

    /** Dias que o doador precisa esperar entre duas doações. */
    public int intervaloMinimoDias() {
        return homem() ? 60 : 90;
    }

    /** Quantidade máxima de doações em 12 meses. */
    public int limiteDoacoesPorAno() {
        return homem() ? 4 : 3;
    }

    /** Idade do doador na data informada. */
    public int idadeEm(LocalDate data) {
        return Period.between(dataNascimento, data).getYears();
    }

    /**
     * Regra: só pode doar quem tem de 16 a 69 anos na data de referência.
     */
    public void validarIdade(LocalDate dataReferencia) {
        int idade = idadeEm(dataReferencia);
        if (idade < IDADE_MINIMA || idade > IDADE_MAXIMA) {
            throw new IdadeNaoPermitidaException(
                    "O doador terá " + idade + " anos em " + dataReferencia.format(DATA_BR)
                            + ". Só é permitido doar sangue entre " + IDADE_MINIMA + " e " + IDADE_MAXIMA + " anos.");
        }
    }

    /**
     * Verifica se o doador pode ter uma doação na data desejada, olhando o histórico dele.
     *
     * @param dataDesejada dia da nova doação
     * @param historico    as outras doações do doador (sem a que está sendo criada/alterada)
     */
    public void validarNovaDoacao(LocalDate dataDesejada, List<Doacao> historico) {
        validarIdade(dataDesejada);

        int intervalo = intervaloMinimoDias();
        String grupo = homem() ? "Homens" : "Mulheres";
        int doacoesEm12Meses = 0;

        for (Doacao doacao : historico) {
            if (!doacao.getStatus().contaParaAsRegras()) {
                continue; // canceladas e faltas não bloqueiam o doador
            }
            LocalDate outraData = doacao.getDataDoacao().toLocalDate();

            // Regra do intervalo mínimo (60 ou 90 dias)
            long diasDeDiferenca = Math.abs(ChronoUnit.DAYS.between(outraData, dataDesejada));
            if (diasDeDiferenca < intervalo) {
                if (outraData.isAfter(dataDesejada)) {
                    throw new IntervaloMinimoException(
                            "Já existe uma doação marcada para " + outraData.format(DATA_BR) + ". "
                                    + grupo + " devem manter " + intervalo + " dias entre doações.");
                }
                throw new IntervaloMinimoException(
                        "A última doação foi em " + outraData.format(DATA_BR) + ". "
                                + grupo + " devem aguardar " + intervalo + " dias. Próxima data permitida: "
                                + outraData.plusDays(intervalo).format(DATA_BR) + ".");
            }

            // Conta as doações nos 12 meses anteriores à data desejada
            if (outraData.isAfter(dataDesejada.minusYears(1)) && !outraData.isAfter(dataDesejada)) {
                doacoesEm12Meses++;
            }
        }

        // Regra do limite anual (4 ou 3 doações)
        if (doacoesEm12Meses >= limiteDoacoesPorAno()) {
            throw new LimiteAnualException(
                    grupo + " podem doar no máximo " + limiteDoacoesPorAno() + " vezes em 12 meses. "
                            + "O doador já tem " + doacoesEm12Meses + " doações nesse período.");
        }
    }

    /**
     * Calcula a partir de que dia o doador pode doar de novo ("Posso doar?").
     * Sem doações anteriores, a resposta é hoje.
     */
    public LocalDate proximaDataPermitida(List<Doacao> historico, LocalDate hoje) {
        LocalDate proxima = hoje;
        for (Doacao doacao : historico) {
            if (doacao.getStatus().contaParaAsRegras()) {
                LocalDate liberacao = doacao.getDataDoacao().toLocalDate().plusDays(intervaloMinimoDias());
                if (liberacao.isAfter(proxima)) {
                    proxima = liberacao;
                }
            }
        }
        return proxima;
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(String tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }
}
