package com.enzobf.cliente_pedido_kafka.strategy;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public interface StatusPedidoStrategy {

    boolean podeTransicionar(StatusPedido atual, StatusPedido novo);
}
