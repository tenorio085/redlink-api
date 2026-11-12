package br.com.fiap.redlink.controller;

import br.com.fiap.redlink.dao.DoacaoDao;
import br.com.fiap.redlink.dao.DoadorDao;
import br.com.fiap.redlink.dao.HemocentroDao;
import br.com.fiap.redlink.dto.DoacaoResponseDTO;
import br.com.fiap.redlink.dto.DoacaoUpdateDTO;
import br.com.fiap.redlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.redlink.model.Doacao;
import br.com.fiap.redlink.model.Doador;
import br.com.fiap.redlink.model.Hemocentro;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta, atualização e remoção de doações.
 * O cadastro (agendamento) fica na rota aninhada POST /doadores/{id}/doacoes,
 * porque toda doação pertence a um doador.
 */
@RestController
@RequestMapping("/doacoes")
public class DoacaoController {

    private final DoacaoDao doacaoDao;
    private final DoadorDao doadorDao;
    private final HemocentroDao hemocentroDao;
    private final ModelMapper mapper;

    public DoacaoController(DoacaoDao doacaoDao, DoadorDao doadorDao,
                            HemocentroDao hemocentroDao, ModelMapper mapper) {
        this.doacaoDao = doacaoDao;
        this.doadorDao = doadorDao;
        this.hemocentroDao = hemocentroDao;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DoacaoResponseDTO> listar() {
        return doacaoDao.listar().stream()
                .map(doacao -> mapper.map(doacao, DoacaoResponseDTO.class))
                .toList();
    }

    @GetMapping("/{id}")
    public DoacaoResponseDTO buscar(@PathVariable Long id) {
        return mapper.map(buscarOuFalhar(id), DoacaoResponseDTO.class);
    }

    /**
     * Remarca a doação, troca o hemocentro ou muda o status
     * (REALIZADA, NAO_COMPARECEU, CANCELADA).
     */
    @PutMapping("/{id}")
    public DoacaoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid DoacaoUpdateDTO dto) {
        Doacao atual = buscarOuFalhar(id);
        Hemocentro hemocentro = hemocentroDao.buscarPorId(dto.getHemocentroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hemocentro", dto.getHemocentroId()));

        Doacao doacao = mapper.map(dto, Doacao.class);
        doacao.setId(id);
        doacao.setDoador(atual.getDoador());
        doacao.setHemocentro(hemocentro);

        // Se a doação continua valendo (agendada ou realizada), as regras são conferidas
        // de novo contra as OUTRAS doações do mesmo doador.
        if (doacao.getStatus().contaParaAsRegras()) {
            Long doadorId = atual.getDoador().getId();
            Doador doador = doadorDao.buscarPorId(doadorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Doador", doadorId));
            List<Doacao> outras = doacaoDao.listarPorDoador(doadorId).stream()
                    .filter(outra -> !outra.getId().equals(id))
                    .toList();
            doador.validarNovaDoacao(doacao.getDataDoacao().toLocalDate(), outras);
        }

        doacaoDao.atualizar(doacao);
        return mapper.map(doacao, DoacaoResponseDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        buscarOuFalhar(id);
        doacaoDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    private Doacao buscarOuFalhar(Long id) {
        return doacaoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação", id));
    }
}
