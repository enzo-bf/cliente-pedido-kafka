package com.enzobf.cliente_pedido_kafka.exception;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado com o ID: " + id);
    }
}