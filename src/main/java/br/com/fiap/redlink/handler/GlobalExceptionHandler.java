package br.com.fiap.redlink.handler;

import br.com.fiap.redlink.dto.ErroDTO;
import br.com.fiap.redlink.exception.RecursoNaoEncontradoException;
import br.com.fiap.redlink.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Handler global: todo erro da API passa por aqui e sai no mesmo formato JSON
 * ({@link ErroDTO}), sem stack trace e sem detalhes do SQL.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400 - o JSON chegou, mas algum campo não passou no Bean Validation. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> validacao(MethodArgumentNotValidException ex) {
        List<ErroDTO.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDTO.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ErroDTO.deValidacao("Um ou mais campos estão inválidos. Veja a lista em \"campos\".", campos));
    }

    /** 400 - corpo vazio, JSON mal formado ou data em formato errado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Não foi possível ler o corpo da requisição. Envie um JSON válido, com datas no formato "
                        + "2026-11-25 (data) ou 2026-11-25T10:00:00 (data e hora).");
    }

    /** 400 - id da rota que não é número, por exemplo /doadores/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor informado para \"" + ex.getName() + "\" é inválido. O id deve ser um número.");
    }

    /** 404 - doador, doação ou hemocentro que não existe. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> naoEncontrado(RecursoNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    /** 404 - rota que não existe na API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroDTO> rotaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada", "Esta rota não existe na API.");
    }

    /** 405 - método HTTP não aceito na rota, por exemplo DELETE /doadores. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroDTO> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + ex.getMethod() + " não é aceito nesta rota.");
    }

    /** 415 - corpo enviado sem Content-Type: application/json. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroDTO> tipoNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado",
                "Envie o corpo como JSON, com o cabeçalho Content-Type: application/json.");
    }

    /** 409 - qualquer regra de negócio violada (intervalo mínimo, limite anual, idade...). */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroDTO> regraDeNegocio(RegraNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getTitulo(), ex.getMessage());
    }

    /** 500 - erro inesperado (inclusive do banco). O detalhe vai só para o log do servidor. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em alguns instantes.");
    }

    private ResponseEntity<ErroDTO> responder(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(ErroDTO.de(status.value(), erro, mensagem));
    }
}
