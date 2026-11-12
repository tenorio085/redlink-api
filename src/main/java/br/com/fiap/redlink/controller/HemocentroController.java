package br.com.fiap.redlink.controller;

import br.com.fiap.redlink.dao.DoacaoDao;
import br.com.fiap.redlink.dao.HemocentroDao;
import br.com.fiap.redlink.dto.HemocentroRequestDTO;
import br.com.fiap.redlink.dto.HemocentroResponseDTO;
import br.com.fiap.redlink.exception.HemocentroComDoacoesException;
import br.com.fiap.redlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.redlink.model.Hemocentro;
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
import java.util.List;

/**
 * CRUD de hemocentros.
 */
@RestController
@RequestMapping("/hemocentros")
public class HemocentroController {

    private final HemocentroDao hemocentroDao;
    private final DoacaoDao doacaoDao;
    private final ModelMapper mapper;

    public HemocentroController(HemocentroDao hemocentroDao, DoacaoDao doacaoDao, ModelMapper mapper) {
        this.hemocentroDao = hemocentroDao;
        this.doacaoDao = doacaoDao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<HemocentroResponseDTO> cadastrar(@RequestBody @Valid HemocentroRequestDTO dto) {
        Hemocentro hemocentro = hemocentroDao.inserir(mapper.map(dto, Hemocentro.class));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(hemocentro.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.map(hemocentro, HemocentroResponseDTO.class));
    }

    @GetMapping
    public List<HemocentroResponseDTO> listar() {
        return hemocentroDao.listar().stream()
                .map(hemocentro -> mapper.map(hemocentro, HemocentroResponseDTO.class))
                .toList();
    }

    @GetMapping("/{id}")
    public HemocentroResponseDTO buscar(@PathVariable Long id) {
        return mapper.map(buscarOuFalhar(id), HemocentroResponseDTO.class);
    }

    @PutMapping("/{id}")
    public HemocentroResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid HemocentroRequestDTO dto) {
        buscarOuFalhar(id);

        Hemocentro hemocentro = mapper.map(dto, Hemocentro.class);
        hemocentro.setId(id);
        hemocentroDao.atualizar(hemocentro);
        return mapper.map(hemocentro, HemocentroResponseDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        buscarOuFalhar(id);

        // Regra: hemocentro que já recebeu doações não pode ser removido
        long doacoes = doacaoDao.contarPorHemocentro(id);
        if (doacoes > 0) {
            throw new HemocentroComDoacoesException(doacoes);
        }
        hemocentroDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    private Hemocentro buscarOuFalhar(Long id) {
        return hemocentroDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hemocentro", id));
    }
}
