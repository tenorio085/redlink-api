package br.com.fiap.redlink.dao;

import br.com.fiap.redlink.exception.ErroBancoException;
import br.com.fiap.redlink.model.Doacao;
import br.com.fiap.redlink.model.Doador;
import br.com.fiap.redlink.model.Hemocentro;
import br.com.fiap.redlink.model.StatusDoacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_rl_doacao com JDBC puro.
 * As consultas já trazem o nome do doador e do hemocentro (JOIN) para a resposta da API.
 */
@Repository
public class DoacaoDao {

    private static final String SELECT_BASE =
            "SELECT c.cd_doacao, c.dt_doacao, c.ds_status, "
                    + "c.cd_doador, d.nm_doador, d.sg_sexo, d.dt_nascimento, "
                    + "c.cd_hemocentro, h.nm_hemocentro, h.ds_endereco, h.nm_cidade "
                    + "FROM tb_rl_doacao c "
                    + "INNER JOIN tb_rl_doador d ON d.cd_doador = c.cd_doador "
                    + "INNER JOIN tb_rl_hemocentro h ON h.cd_hemocentro = c.cd_hemocentro ";

    private final DataSource dataSource;

    public DoacaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Doacao inserir(Doacao doacao) {
        String sqlId = "SELECT sq_tb_rl_doacao.NEXTVAL FROM dual";
        String sql = "INSERT INTO tb_rl_doacao (cd_doacao, dt_doacao, ds_status, cd_doador, cd_hemocentro) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            // 1) pega o próximo código na sequence
            try (PreparedStatement stmt = conexao.prepareStatement(sqlId);
                 ResultSet rs = stmt.executeQuery()) {
                rs.next();
                doacao.setId(rs.getLong(1));
            }
            // 2) grava a linha, com a chave estrangeira do doador (1:N)
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setLong(1, doacao.getId());
                stmt.setTimestamp(2, Timestamp.valueOf(doacao.getDataDoacao()));
                stmt.setString(3, doacao.getStatus().name());
                stmt.setLong(4, doacao.getDoador().getId());
                stmt.setLong(5, doacao.getHemocentro().getId());
                stmt.executeUpdate();
            }
            return doacao;
        } catch (SQLException e) {
            throw new ErroBancoException("cadastrar doação", e);
        }
    }

    public List<Doacao> listar() {
        return consultarLista(SELECT_BASE + "ORDER BY c.dt_doacao DESC", null, "listar doações");
    }

    /** Lado N do relacionamento: todas as doações de um doador. */
    public List<Doacao> listarPorDoador(Long doadorId) {
        return consultarLista(SELECT_BASE + "WHERE c.cd_doador = ? ORDER BY c.dt_doacao DESC",
                doadorId, "listar doações do doador");
    }

    public Optional<Doacao> buscarPorId(Long id) {
        List<Doacao> resultado = consultarLista(SELECT_BASE + "WHERE c.cd_doacao = ?", id, "buscar doação");
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public long contarPorDoador(Long doadorId) {
        return contar("SELECT COUNT(*) FROM tb_rl_doacao WHERE cd_doador = ?", doadorId);
    }

    public long contarPorHemocentro(Long hemocentroId) {
        return contar("SELECT COUNT(*) FROM tb_rl_doacao WHERE cd_hemocentro = ?", hemocentroId);
    }

    public void atualizar(Doacao doacao) {
        String sql = "UPDATE tb_rl_doacao SET dt_doacao = ?, ds_status = ?, cd_hemocentro = ? WHERE cd_doacao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(doacao.getDataDoacao()));
            stmt.setString(2, doacao.getStatus().name());
            stmt.setLong(3, doacao.getHemocentro().getId());
            stmt.setLong(4, doacao.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("atualizar doação", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM tb_rl_doacao WHERE cd_doacao = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("remover doação", e);
        }
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------

    /** Executa um SELECT com zero ou um parâmetro numérico e monta a lista de doações. */
    private List<Doacao> consultarLista(String sql, Long parametro, String operacao) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setLong(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Doacao> doacoes = new ArrayList<>();
                while (rs.next()) {
                    doacoes.add(montar(rs));
                }
                return doacoes;
            }
        } catch (SQLException e) {
            throw new ErroBancoException(operacao, e);
        }
    }

    private long contar(String sql, Long parametro) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("contar doações", e);
        }
    }

    /** Transforma a linha atual do ResultSet em um objeto do domínio. */
    private Doacao montar(ResultSet rs) throws SQLException {
        Doador doador = new Doador();
        doador.setId(rs.getLong("cd_doador"));
        doador.setNome(rs.getString("nm_doador"));
        doador.setSexo(rs.getString("sg_sexo"));
        doador.setDataNascimento(rs.getDate("dt_nascimento").toLocalDate());

        Hemocentro hemocentro = new Hemocentro(
                rs.getLong("cd_hemocentro"),
                rs.getString("nm_hemocentro"),
                rs.getString("ds_endereco"),
                rs.getString("nm_cidade"));

        Doacao doacao = new Doacao();
        doacao.setId(rs.getLong("cd_doacao"));
        doacao.setDataDoacao(rs.getTimestamp("dt_doacao").toLocalDateTime());
        doacao.setStatus(StatusDoacao.valueOf(rs.getString("ds_status")));
        doacao.setDoador(doador);
        doacao.setHemocentro(hemocentro);
        return doacao;
    }
}
