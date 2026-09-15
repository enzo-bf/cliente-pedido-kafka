package com.enzobf.cliente_pedido_kafka.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.enzobf.cliente_pedido_kafka.dto.response.ErroResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ValorInvalidoException.class,
            DescontoInvalidoException.class,
            StatusPedidoInvalidoException.class,
            ClientePossuiPedidosException.class
    })
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RuntimeException exception) {
        log.warn("Regra de negócio violada: {}", exception.getMessage());
        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({
            CpfJaCadastradoException.class,
            EmailJaCadastradoException.class
    })
    public ResponseEntity<ErroResponse> tratarConflito(RuntimeException exception) {
        log.warn("Conflito de cadastro: {}", exception.getMessage());
        return resposta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({
            ClienteNaoEncontradoException.class,
            PedidoNaoEncontradoException.class
    })
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RuntimeException exception) {
        log.warn("Recurso não encontrado: {}", exception.getMessage());
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarErrosDeValidacao(MethodArgumentNotValidException exception) {
        String mensagens = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Falha de validação: {}", mensagens);
        return resposta(HttpStatus.BAD_REQUEST, mensagens);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception exception) {
        log.error("Erro inesperado", exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        ErroResponse erro = new ErroResponse(LocalDateTime.now(), status.value(), mensagem);
        return ResponseEntity.status(status).body(erro);
    }
}
