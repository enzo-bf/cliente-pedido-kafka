package com.enzobf.cliente_pedido_kafka.exception;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException() {
        super("O valor do pedido deve ser maior que zero");
    }
}