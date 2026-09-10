package com.enzobf.cliente_pedido_kafka.exception;

public class CpfJaCadastradoException extends RuntimeException {

    public CpfJaCadastradoException(String cpf) {
        super("Já existe um cliente cadastrado com o CPF: " + cpf);
    }
}