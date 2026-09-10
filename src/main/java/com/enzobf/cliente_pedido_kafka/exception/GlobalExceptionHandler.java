package com.enzobf.cliente_pedido_kafka.exception;

import com.enzobf.cliente_pedido_kafka.dto.response.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CpfJaCadastradoException.class)
    public ResponseEntity<ErroResponse> tratarCpfJaCadastrado(
            CpfJaCadastradoException exception
    ) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(status)
                .body(erro);
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarClienteNaoEncontrado(
            ClienteNaoEncontradoException exception
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(status)
                .body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarErrosDeValidacao(
            MethodArgumentNotValidException exception
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagens = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                mensagens
        );

        return ResponseEntity
                .status(status)
                .body(erro);
    }
}
