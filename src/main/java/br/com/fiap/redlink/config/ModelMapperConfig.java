package br.com.fiap.redlink.config;

import br.com.fiap.redlink.dto.DoacaoResponseDTO;
import br.com.fiap.redlink.model.Doacao;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cria o ModelMapper usado pelos controllers para converter DTO em model e model em DTO.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // STRICT: só copia campos com exatamente o mesmo nome.
        // Evita que "hemocentroId" seja copiado por engano para o "id" da doação.
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Na resposta da doação, os dados do doador e do hemocentro são "achatados":
        // doacao.doador.nome -> doadorNome, doacao.hemocentro.id -> hemocentroId...
        mapper.typeMap(Doacao.class, DoacaoResponseDTO.class).addMappings(m -> {
            m.map(doacao -> doacao.getDoador().getId(), DoacaoResponseDTO::setDoadorId);
            m.map(doacao -> doacao.getDoador().getNome(), DoacaoResponseDTO::setDoadorNome);
            m.map(doacao -> doacao.getHemocentro().getId(), DoacaoResponseDTO::setHemocentroId);
            m.map(doacao -> doacao.getHemocentro().getNome(), DoacaoResponseDTO::setHemocentroNome);
        });

        return mapper;
    }
}
