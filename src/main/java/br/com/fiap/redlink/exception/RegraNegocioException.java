package br.com.fiap.redlink.exception;

/**
 * Classe base das regras de negócio violadas. O handler global
 * transforma qualquer filha desta classe em HTTP 409 (Conflict).
 */
public abstract class RegraNegocioException extends RuntimeException {

    private final String titulo;

    protected RegraNegocioException(String titulo, String mensagem) {
        super(mensagem);
        this.titulo = titulo;
    }

    /** Texto curto que vai no campo "erro" do JSON. */
    public String getTitulo() {
        return titulo;
    }
}
