package com.enzobf.cliente_pedido_kafka.exception;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public class StatusPedidoInvalidoException extends RuntimeException {

    public StatusPedidoInvalidoException(StatusPedido atual, StatusPedido novo) {
        super("Não é permitido alterar o status de " + atual + " para " + novo);
    }
}
