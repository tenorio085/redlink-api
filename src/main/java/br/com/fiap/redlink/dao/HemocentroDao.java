package br.com.fiap.redlink.dao;

import br.com.fiap.redlink.exception.ErroBancoException;
import br.com.fiap.redlink.model.Hemocentro;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_rl_hemocentro com JDBC puro.
 */
@Repository
public class HemocentroDao {

    private static final String COLUNAS = "cd_hemocentro, nm_hemocentro, ds_endereco, nm_cidade";

    private final DataSource dataSource;

    public HemocentroDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Hemocentro inserir(Hemocentro hemocentro) {
        String sqlId = "SELECT sq_tb_rl_hemocentro.NEXTVAL FROM dual";
        String sql = "INSERT INTO tb_rl_hemocentro (" + COLUNAS + ") VALUES (?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            // 1) pega o próximo código na sequence
            try (PreparedStatement stmt = conexao.prepareStatement(sqlId);
                 ResultSet rs = stmt.executeQuery()) {
                rs.next();
                hemocentro.setId(rs.getLong(1));
            }
            // 2) grava a linha
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setLong(1, hemocentro.getId());
                stmt.setString(2, hemocentro.getNome());
                stmt.setString(3, hemocentro.getEndereco());
                stmt.setString(4, hemocentro.getCidade());
                stmt.executeUpdate();
            }
            return hemocentro;
        } catch (SQLException e) {
            throw new ErroBancoException("cadastrar hemocentro", e);
        }
    }

    public List<Hemocentro> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_rl_hemocentro ORDER BY nm_hemocentro";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Hemocentro> hemocentros = new ArrayList<>();
            while (rs.next()) {
                hemocentros.add(montar(rs));
            }
            return hemocentros;
        } catch (SQLException e) {
            throw new ErroBancoException("listar hemocentros", e);
        }
    }

    public Optional<Hemocentro> buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_rl_hemocentro WHERE cd_hemocentro = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("buscar hemocentro", e);
        }
    }

    public void atualizar(Hemocentro hemocentro) {
        String sql = "UPDATE tb_rl_hemocentro SET nm_hemocentro = ?, ds_endereco = ?, nm_cidade = ? "
                + "WHERE cd_hemocentro = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, hemocentro.getNome());
            stmt.setString(2, hemocentro.getEndereco());
            stmt.setString(3, hemocentro.getCidade());
            stmt.setLong(4, hemocentro.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("atualizar hemocentro", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM tb_rl_hemocentro WHERE cd_hemocentro = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("remover hemocentro", e);
        }
    }

    /** Transforma a linha atual do ResultSet em um objeto do domínio. */
    private Hemocentro montar(ResultSet rs) throws SQLException {
        return new Hemocentro(
                rs.getLong("cd_hemocentro"),
                rs.getString("nm_hemocentro"),
                rs.getString("ds_endereco"),
                rs.getString("nm_cidade"));
    }
}
