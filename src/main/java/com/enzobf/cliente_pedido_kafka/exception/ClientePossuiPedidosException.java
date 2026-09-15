package com.enzobf.cliente_pedido_kafka.exception;

public class ClientePossuiPedidosException extends RuntimeException {

    public ClientePossuiPedidosException(Long id) {
        super("Não é possível excluir o cliente " + id + " pois existem pedidos vinculados");
    }
}
