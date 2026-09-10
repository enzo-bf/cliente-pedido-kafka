package com.enzobf.cliente_pedido_kafka.exception;

public class DescontoInvalidoException extends RuntimeException {

    public DescontoInvalidoException() {
        super("O desconto deve estar entre 0% e 20%");
    }
}