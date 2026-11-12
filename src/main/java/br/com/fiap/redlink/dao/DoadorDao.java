package br.com.fiap.redlink.dao;

import br.com.fiap.redlink.exception.ErroBancoException;
import br.com.fiap.redlink.model.Doador;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_rl_doador com JDBC puro.
 * <p>
 * No banco, o doador guarda o código do tipo sanguíneo (cd_tipo, chave estrangeira
 * para tb_rl_tipo_sanguineo). No domínio trabalhamos com o texto ("O-", "A+"...),
 * então o DAO faz essa tradução com um JOIN na leitura e um subselect na gravação.
 */
@Repository
public class DoadorDao {

    private static final String SELECT_BASE =
            "SELECT d.cd_doador, d.nm_doador, d.nr_cpf, d.sg_sexo, d.dt_nascimento, d.nr_peso, "
                    + "d.ds_email, d.nr_telefone, t.ds_tipo "
                    + "FROM tb_rl_doador d "
                    + "INNER JOIN tb_rl_tipo_sanguineo t ON t.cd_tipo = d.cd_tipo ";

    private final DataSource dataSource;

    public DoadorDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Doador inserir(Doador doador) {
        String sqlId = "SELECT sq_tb_rl_doador.NEXTVAL FROM dual";
        String sql = "INSERT INTO tb_rl_doador "
                + "(cd_doador, nm_doador, nr_cpf, sg_sexo, dt_nascimento, nr_peso, ds_email, nr_telefone, cd_tipo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT cd_tipo FROM tb_rl_tipo_sanguineo WHERE ds_tipo = ?))";
        try (Connection conexao = dataSource.getConnection()) {
            // 1) pega o próximo código na sequence
            try (PreparedStatement stmt = conexao.prepareStatement(sqlId);
                 ResultSet rs = stmt.executeQuery()) {
                rs.next();
                doador.setId(rs.getLong(1));
            }
            // 2) grava a linha
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setLong(1, doador.getId());
                stmt.setString(2, doador.getNome());
                stmt.setString(3, doador.getCpf());
                stmt.setString(4, doador.getSexo());
                stmt.setDate(5, Date.valueOf(doador.getDataNascimento()));
                stmt.setBigDecimal(6, doador.getPeso());
                stmt.setString(7, doador.getEmail());
                stmt.setString(8, doador.getTelefone());
                stmt.setString(9, doador.getTipoSanguineo());
                stmt.executeUpdate();
            }
            return doador;
        } catch (SQLException e) {
            throw new ErroBancoException("cadastrar doador", e);
        }
    }

    public List<Doador> listar() {
        String sql = SELECT_BASE + "ORDER BY d.nm_doador";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Doador> doadores = new ArrayList<>();
            while (rs.next()) {
                doadores.add(montar(rs));
            }
            return doadores;
        } catch (SQLException e) {
            throw new ErroBancoException("listar doadores", e);
        }
    }

    public Optional<Doador> buscarPorId(Long id) {
        String sql = SELECT_BASE + "WHERE d.cd_doador = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(montar(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("buscar doador", e);
        }
    }

    /**
     * Diz se o CPF já pertence a OUTRO doador.
     *
     * @param ignorarId id do próprio doador numa atualização (null no cadastro)
     */
    public boolean existeCpf(String cpf, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_rl_doador WHERE nr_cpf = ? AND cd_doador <> ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            stmt.setLong(2, ignorarId == null ? -1L : ignorarId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new ErroBancoException("verificar CPF", e);
        }
    }

    public void atualizar(Doador doador) {
        String sql = "UPDATE tb_rl_doador SET nm_doador = ?, nr_cpf = ?, sg_sexo = ?, dt_nascimento = ?, "
                + "nr_peso = ?, ds_email = ?, nr_telefone = ?, "
                + "cd_tipo = (SELECT cd_tipo FROM tb_rl_tipo_sanguineo WHERE ds_tipo = ?) "
                + "WHERE cd_doador = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, doador.getNome());
            stmt.setString(2, doador.getCpf());
            stmt.setString(3, doador.getSexo());
            stmt.setDate(4, Date.valueOf(doador.getDataNascimento()));
            stmt.setBigDecimal(5, doador.getPeso());
            stmt.setString(6, doador.getEmail());
            stmt.setString(7, doador.getTelefone());
            stmt.setString(8, doador.getTipoSanguineo());
            stmt.setLong(9, doador.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("atualizar doador", e);
        }
    }

    public void remover(Long id) {
        String sql = "DELETE FROM tb_rl_doador WHERE cd_doador = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErroBancoException("remover doador", e);
        }
    }

    /** Transforma a linha atual do ResultSet em um objeto do domínio. */
    private Doador montar(ResultSet rs) throws SQLException {
        Doador doador = new Doador();
        doador.setId(rs.getLong("cd_doador"));
        doador.setNome(rs.getString("nm_doador"));
        doador.setCpf(rs.getString("nr_cpf"));
        doador.setSexo(rs.getString("sg_sexo"));
        doador.setDataNascimento(rs.getDate("dt_nascimento").toLocalDate());
        doador.setPeso(rs.getBigDecimal("nr_peso"));
        doador.setEmail(rs.getString("ds_email"));
        doador.setTelefone(rs.getString("nr_telefone"));
        doador.setTipoSanguineo(rs.getString("ds_tipo"));
        return doador;
    }
}
