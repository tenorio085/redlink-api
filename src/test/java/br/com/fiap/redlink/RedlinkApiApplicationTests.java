package br.com.fiap.redlink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de ponta a ponta da API (controller -> DAO -> banco), usando um H2 em memória
 * no modo Oracle, criado a partir do script sql/redlink.sql.
 * O script já cadastra o hemocentro de id 1.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RedlinkApiApplicationTests {

    @Autowired
    private MockMvc mvc;

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private String doadorJson(String nome, String cpf, String sexo, String nascimento, String peso) {
        return """
                {"nome":"%s","cpf":"%s","sexo":"%s","dataNascimento":"%s","peso":%s,
                 "email":"teste@redlink.com","telefone":"11999990000","tipoSanguineo":"O-"}
                """.formatted(nome, cpf, sexo, nascimento, peso);
    }

    /** Cadastra um doador e devolve a rota dele (ex.: /doadores/3). */
    private String cadastrarDoador(String cpf, String sexo) throws Exception {
        MvcResult resultado = mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("Doador Teste", cpf, sexo, "1998-05-10", "72.5")))
                .andExpect(status().isCreated())
                .andReturn();
        String location = resultado.getResponse().getHeader("Location");
        return location.substring(location.indexOf("/doadores/"));
    }

    private String doacaoJson(int diasAPartirDeHoje) {
        LocalDateTime data = LocalDateTime.now().plusDays(diasAPartirDeHoje).truncatedTo(ChronoUnit.MINUTES);
        return "{\"dataDoacao\":\"" + data + ":00\",\"hemocentroId\":1}";
    }

    @Test
    void rotaInicialMostraQueAApiEstaNoAr() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"))
                .andExpect(jsonPath("$.rotas", hasSize(greaterThanOrEqualTo(3))));
    }

    // ------------------------------------------------------------------
    // Doadores
    // ------------------------------------------------------------------

    @Test
    void cadastraEBuscaDoadorSemExporOCpf() throws Exception {
        MvcResult resultado = mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("João Silva", "11111111111", "M", "1998-05-10", "72.5")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/doadores/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.tipoSanguineo").value("O-"))
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andReturn();

        String location = resultado.getResponse().getHeader("Location");
        String rota = location.substring(location.indexOf("/doadores/"));

        mvc.perform(get(rota))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataNascimento").value("1998-05-10"))
                .andExpect(jsonPath("$.peso").value(72.5));

        mvc.perform(get("/doadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void atualizaDoador() throws Exception {
        String rota = cadastrarDoador("22222222222", "F");

        mvc.perform(put(rota).contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("Camila Souza", "22222222222", "F", "1991-01-20", "60")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Camila Souza"));

        mvc.perform(get(rota)).andExpect(jsonPath("$.nome").value("Camila Souza"));
    }

    @Test
    void cadastroInvalidoDevolve400ComOsCampos() throws Exception {
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("", "123", "X", "1998-05-10", "45")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    void jsonMalFormadoDevolve400() throws Exception {
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON).content("{isso não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void idInexistenteDevolve404() throws Exception {
        mvc.perform(get("/doadores/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem", containsString("99999")));
    }

    @Test
    void idNaoNumericoDevolve400ERotaInexistenteDevolve404() throws Exception {
        mvc.perform(get("/doadores/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/nao-existe")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Rota não encontrada"));
    }

    @Test
    void metodoNaoSuportadoDevolve405() throws Exception {
        mvc.perform(delete("/doadores"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void cpfRepetidoDevolve409() throws Exception {
        cadastrarDoador("33333333333", "M");
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("Outro", "33333333333", "M", "1990-01-01", "80")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("CPF já cadastrado"));
    }

    @Test
    void idadeForaDaFaixaDevolve409() throws Exception {
        String nascimento = java.time.LocalDate.now().minusYears(10).toString();
        mvc.perform(post("/doadores").contentType(MediaType.APPLICATION_JSON)
                        .content(doadorJson("Criança", "44444444444", "M", nascimento, "55")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Idade não permitida para doação"));
    }

    // ------------------------------------------------------------------
    // Doações (1:N) e regras de negócio
    // ------------------------------------------------------------------

    @Test
    void agendaDoacaoEBloqueiaIntervaloMinimo() throws Exception {
        String rota = cadastrarDoador("55555555555", "M");

        // antes de qualquer doação, o doador já pode doar
        mvc.perform(get(rota + "/proxima-doacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.podeDoar").value(true));

        // 1ª doação: daqui a 5 dias
        MvcResult criada = mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(5)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/doacoes/")))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.doadorNome").value("Doador Teste"))
                .andExpect(jsonPath("$.hemocentroId").value(1))
                .andExpect(jsonPath("$.hemocentroNome").value("Hemocentro Central"))
                .andReturn();
        String location = criada.getResponse().getHeader("Location");
        String rotaDoacao = location.substring(location.indexOf("/doacoes/"));

        // 2ª doação 30 dias depois da 1ª: homem precisa esperar 60 -> 409
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(35)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Intervalo mínimo não cumprido"))
                .andExpect(jsonPath("$.mensagem", containsString("Homens devem aguardar 60 dias")))
                .andExpect(jsonPath("$.timestamp").exists());

        // 61 dias depois da 1ª já pode
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(66)))
                .andExpect(status().isCreated());

        mvc.perform(get(rota + "/doacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get(rota + "/proxima-doacao"))
                .andExpect(jsonPath("$.podeDoar").value(false))
                .andExpect(jsonPath("$.intervaloMinimoDias").value(60));

        // doador com doações não pode ser removido
        mvc.perform(delete(rota))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Doador possui doações"));

        // busca, atualização de status e remoção da doação
        mvc.perform(get(rotaDoacao)).andExpect(status().isOk());
        LocalDateTime data = LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MINUTES);
        mvc.perform(put(rotaDoacao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataDoacao\":\"" + data + ":00\",\"hemocentroId\":1,\"status\":\"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.id").value(Integer.parseInt(rotaDoacao.substring("/doacoes/".length()))));
        mvc.perform(put(rotaDoacao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataDoacao\":\"" + data + ":00\",\"hemocentroId\":1,\"status\":\"QUALQUER\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete(rotaDoacao)).andExpect(status().isNoContent());
        mvc.perform(get(rotaDoacao)).andExpect(status().isNotFound());
    }

    @Test
    void mulherRespeita90DiasELimiteDe3DoacoesPorAno() throws Exception {
        String rota = cadastrarDoador("66666666666", "F");

        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(10)))
                .andExpect(status().isCreated());
        // 70 dias depois: mulher precisa de 90
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(80)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Mulheres devem aguardar 90 dias")));

        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(101)))
                .andExpect(status().isCreated());
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(192)))
                .andExpect(status().isCreated());
        // 4ª doação em 12 meses: bloqueada pelo limite anual
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(283)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Limite anual de doações atingido"));
    }

    @Test
    void agendamentoInvalidoDevolve400E404() throws Exception {
        String rota = cadastrarDoador("77777777777", "M");

        // data no passado
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON).content(doacaoJson(-3)))
                .andExpect(status().isBadRequest());
        // hemocentro que não existe
        LocalDateTime data = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MINUTES);
        mvc.perform(post(rota + "/doacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataDoacao\":\"" + data + ":00\",\"hemocentroId\":999}"))
                .andExpect(status().isNotFound());
        // doador que não existe
        mvc.perform(get("/doadores/99999/doacoes")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Hemocentros
    // ------------------------------------------------------------------

    @Test
    void crudDeHemocentro() throws Exception {
        MvcResult criado = mvc.perform(post("/hemocentros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Hemocentro Norte\",\"endereco\":\"Rua A, 10\",\"cidade\":\"Campinas\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/hemocentros/3")))
                .andReturn();
        String location = criado.getResponse().getHeader("Location");
        String rota = location.substring(location.indexOf("/hemocentros/"));

        mvc.perform(put(rota).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Hemocentro Norte\",\"endereco\":\"Rua B, 20\",\"cidade\":\"Campinas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco").value("Rua B, 20"));

        mvc.perform(get("/hemocentros")).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        mvc.perform(delete(rota)).andExpect(status().isNoContent());
        mvc.perform(get(rota)).andExpect(status().isNotFound());

        mvc.perform(post("/hemocentros").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
