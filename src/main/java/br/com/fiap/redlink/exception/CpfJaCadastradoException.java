package br.com.fiap.redlink.exception;

/** Tentativa de cadastrar dois doadores com o mesmo CPF. */
public class CpfJaCadastradoException extends RegraNegocioException {

    public CpfJaCadastradoException() {
        super("CPF já cadastrado", "Já existe um doador cadastrado com este CPF.");
    }
}
