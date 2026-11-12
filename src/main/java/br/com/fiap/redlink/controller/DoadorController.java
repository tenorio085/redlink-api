package br.com.fiap.redlink.controller;

import br.com.fiap.redlink.dao.DoacaoDao;
import br.com.fiap.redlink.dao.DoadorDao;
import br.com.fiap.redlink.dao.HemocentroDao;
import br.com.fiap.redlink.dto.DoacaoRequestDTO;
import br.com.fiap.redlink.dto.DoacaoResponseDTO;
import br.com.fiap.redlink.dto.DoadorRequestDTO;
import br.com.fiap.redlink.dto.DoadorResponseDTO;
import br.com.fiap.redlink.dto.ProximaDoacaoDTO;
import br.com.fiap.redlink.exception.CpfJaCadastradoException;
import br.com.fiap.redlink.exception.DoadorComDoacoesException;
import br.com.fiap.redlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.redlink.model.Doacao;
import br.com.fiap.redlink.model.Doador;
import br.com.fiap.redlink.model.Hemocentro;
import br.com.fiap.redlink.model.StatusDoacao;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CRUD de doadores e as rotas aninhadas do relacionamento 1:N
 * (as doações de um doador).
 */
@RestController
@RequestMapping("/doadores")
public class DoadorController {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DoadorDao doadorDao;
    private final DoacaoDao doacaoDao;
    private final HemocentroDao hemocentroDao;
    private final ModelMapper mapper;

    public DoadorController(DoadorDao doadorDao, DoacaoDao doacaoDao,
                            HemocentroDao hemocentroDao, ModelMapper mapper) {
        this.doadorDao = doadorDao;
        this.doacaoDao = doacaoDao;
        this.hemocentroDao = hemocentroDao;
        this.mapper = mapper;
    }

    // ------------------------------------------------------------------
    // CRUD de doadores
    // ------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<DoadorResponseDTO> cadastrar(@RequestBody @Valid DoadorRequestDTO dto) {
        Doador doador = mapper.map(dto, Doador.class);

        // Regras: idade de 16 a 69 anos e CPF único
        doador.validarIdade(LocalDate.now());
        if (doadorDao.existeCpf(doador.getCpf(), null)) {
            throw new CpfJaCadastradoException();
        }
        doadorDao.inserir(doador);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(doador.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.map(doador, DoadorResponseDTO.class));
    }

    @GetMapping
    public List<DoadorResponseDTO> listar() {
        return doadorDao.listar().stream()
                .map(doador -> mapper.map(doador, DoadorResponseDTO.class))
                .toList();
    }

    @GetMapping("/{id}")
    public DoadorResponseDTO buscar(@PathVariable Long id) {
        return mapper.map(buscarOuFalhar(id), DoadorResponseDTO.class);
    }

    @PutMapping("/{id}")
    public DoadorResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid DoadorRequestDTO dto) {
        buscarOuFalhar(id);

        Doador doador = mapper.map(dto, Doador.class);
        doador.setId(id);
        doador.validarIdade(LocalDate.now());
        if (doadorDao.existeCpf(doador.getCpf(), id)) {
            throw new CpfJaCadastradoException();
        }
        doadorDao.atualizar(doador);
        return mapper.map(doador, DoadorResponseDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        buscarOuFalhar(id);

        // Regra: doador com doações não pode ser removido (o histórico é preservado)
        long doacoes = doacaoDao.contarPorDoador(id);
        if (doacoes > 0) {
            throw new DoadorComDoacoesException(doacoes);
        }
        doadorDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Rotas aninhadas do 1:N: doações de um doador
    // ------------------------------------------------------------------

    @GetMapping("/{id}/doacoes")
    public List<DoacaoResponseDTO> listarDoacoes(@PathVariable Long id) {
        buscarOuFalhar(id);
        return doacaoDao.listarPorDoador(id).stream()
                .map(doacao -> mapper.map(doacao, DoacaoResponseDTO.class))
                .toList();
    }

    /** Agenda uma doação para o doador, aplicando as regras oficiais. */
    @PostMapping("/{id}/doacoes")
    public ResponseEntity<DoacaoResponseDTO> agendarDoacao(@PathVariable Long id,
                                                           @RequestBody @Valid DoacaoRequestDTO dto) {
        Doador doador = buscarOuFalhar(id);
        Hemocentro hemocentro = hemocentroDao.buscarPorId(dto.getHemocentroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hemocentro", dto.getHemocentroId()));

        // Regras: idade na data, intervalo mínimo (60/90 dias) e limite anual (4/3 doações)
        doador.validarNovaDoacao(dto.getDataDoacao().toLocalDate(), doacaoDao.listarPorDoador(id));

        Doacao doacao = mapper.map(dto, Doacao.class);
        doacao.setStatus(StatusDoacao.AGENDADA);
        doacao.setDoador(doador);
        doacao.setHemocentro(hemocentro);
        doacaoDao.inserir(doacao);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/doacoes/{id}").buildAndExpand(doacao.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.map(doacao, DoacaoResponseDTO.class));
    }

    /** "Posso doar?": calcula a partir de que dia o doador pode doar de novo. */
    @GetMapping("/{id}/proxima-doacao")
    public ProximaDoacaoDTO proximaDoacao(@PathVariable Long id) {
        Doador doador = buscarOuFalhar(id);
        LocalDate hoje = LocalDate.now();
        LocalDate proxima = doador.proximaDataPermitida(doacaoDao.listarPorDoador(id), hoje);
        boolean podeDoar = !proxima.isAfter(hoje);

        String mensagem = podeDoar
                ? doador.getNome() + ", você já pode doar sangue. Agende sua doação!"
                : doador.getNome() + ", você poderá doar novamente a partir de " + proxima.format(DATA_BR) + ".";
        return new ProximaDoacaoDTO(id, podeDoar, proxima, doador.intervaloMinimoDias(), mensagem);
    }

    private Doador buscarOuFalhar(Long id) {
        return doadorDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador", id));
    }
}
