# RedLink API

> **O sangue certo, do doador certo, na hora certa.**
> Global Solution 2026/2 · CitizenTech · Domain Driven Design Using Java

API REST da RedLink: cadastra doadores e hemocentros e agenda doações de sangue, bloqueando o
agendamento quando o doador ainda não cumpriu as regras oficiais (intervalo de 60/90 dias,
limite anual e idade de 16 a 69 anos).

## Integrantes

| Nome | RM | Turma |
|------|----|-------|
| Thiago Motitsuki Tan | RM570760 | 1TDSPI |
| Felipe Tenorio Diniz | RM568828 | 1TDSPI |
| João Pedro Rodrigues Siqueira | RM571316 | 1TDSPI |
| Arthur Netto Barbosa | RM569982 | 1TDSPI |
| Gustavo Tenorio de Souza | RM569032 | 1TDSPK |

## Links

- **API no Render:** _PREENCHER (ex.: https://redlink-api.onrender.com)_
- **Vídeo (YouTube, não listado):** _PREENCHER_

## Tecnologias

Java 21 · Spring Boot 4 · JDBC puro (DAO + `PreparedStatement`) · Oracle · Bean Validation ·
ModelMapper · Docker. **Sem** JPA, Hibernate ou Spring Data.

## Como rodar

### 1. Criar as tabelas

Rode o script [`sql/redlink.sql`](sql/redlink.sql) no Oracle da FIAP (SQL Developer). Ele cria as
4 tabelas, as 3 sequences e já insere os 8 tipos sanguíneos e 2 hemocentros de exemplo.

### 2. Informar o banco por variáveis de ambiente

Usuário e senha **não ficam no código**. A API lê três variáveis:

| Variável | O que é |
|----------|---------|
| `DB_URL` | URL JDBC do Oracle (ex.: `jdbc:oracle:thin:@HOST:1521:ORCL`) |
| `DB_USER` | usuário do Oracle |
| `DB_PASSWORD` | senha do Oracle |
| `CORS_ORIGINS` | _(opcional)_ origens liberadas no CORS, separadas por vírgula. Padrão: todas |

No PowerShell:

```powershell
$env:DB_URL = "jdbc:oracle:thin:@HOST:1521:ORCL"
$env:DB_USER = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
.\mvnw.cmd spring-boot:run
```

No IntelliJ: *Run > Edit Configurations > Environment variables*.

A API sobe em `http://localhost:8080`.

### 3. Com Docker (igual ao Render)

```bash
docker build -t redlink-api .
docker run -p 8080:8080 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... redlink-api
```

### Testes

```bash
./mvnw test
```

Os testes sobem a API inteira com um banco H2 em memória (modo Oracle), criado a partir do
mesmo `sql/redlink.sql`. Não precisam do Oracle da FIAP.

## Endpoints

| Método | Rota | Sucesso | Erros |
|--------|------|---------|-------|
| POST | `/doadores` | 201 + Location | 400 · 409 |
| GET | `/doadores` | 200 | — |
| GET | `/doadores/{id}` | 200 | 404 |
| PUT | `/doadores/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/doadores/{id}` | 204 | 404 · 409 |
| GET | `/doadores/{id}/doacoes` | 200 | 404 |
| POST | `/doadores/{id}/doacoes` | 201 + Location | 400 · 404 · 409 |
| GET | `/doadores/{id}/proxima-doacao` | 200 | 404 |
| GET | `/doacoes` | 200 | — |
| GET | `/doacoes/{id}` | 200 | 404 |
| PUT | `/doacoes/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/doacoes/{id}` | 204 | 404 |
| POST | `/hemocentros` | 201 + Location | 400 |
| GET | `/hemocentros` | 200 | — |
| GET | `/hemocentros/{id}` | 200 | 404 |
| PUT | `/hemocentros/{id}` | 200 | 400 · 404 |
| DELETE | `/hemocentros/{id}` | 204 | 404 · 409 |

Método não suportado na rota: 405. Erro inesperado: 500 com mensagem genérica.

### Exemplos

Cadastrar doador — `POST /doadores`

```json
{
  "nome": "João Silva",
  "cpf": "12345678901",
  "sexo": "M",
  "dataNascimento": "1998-05-10",
  "peso": 72.5,
  "email": "joao@email.com",
  "telefone": "11999990000",
  "tipoSanguineo": "O-"
}
```

Agendar doação — `POST /doadores/1/doacoes`

```json
{ "dataDoacao": "2026-11-25T10:00:00", "hemocentroId": 1 }
```

Marcar como realizada — `PUT /doacoes/1`

```json
{ "dataDoacao": "2026-11-25T10:00:00", "hemocentroId": 1, "status": "REALIZADA" }
```

Erro padronizado (409)

```json
{
  "status": 409,
  "erro": "Intervalo mínimo não cumprido",
  "mensagem": "A última doação foi em 25/11/2026. Homens devem aguardar 60 dias. Próxima data permitida: 24/01/2027.",
  "timestamp": "2026-11-11T14:32:00"
}
```

## Estrutura

```
src/main/java/br/com/fiap/redlink
├── model       entidades do domínio e as regras de doação (Doador, Doacao, Hemocentro)
├── dao         acesso ao Oracle com JDBC (um DAO por entidade)
├── dto         entrada (com validação) e saída da API
├── controller  rotas REST e status HTTP
├── exception   erros do domínio
├── handler     @RestControllerAdvice: JSON de erro padronizado
└── config      CORS e ModelMapper
```

A documentação técnica completa está em [`docs/documentacao.md`](docs/documentacao.md).
