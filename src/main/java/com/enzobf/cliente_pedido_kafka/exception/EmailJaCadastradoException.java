package com.enzobf.cliente_pedido_kafka.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Já existe um cliente cadastrado com o e-mail: " + email);
    }
}
