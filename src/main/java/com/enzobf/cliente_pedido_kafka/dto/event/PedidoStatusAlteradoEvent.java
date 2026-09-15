package com.enzobf.cliente_pedido_kafka.dto.event;

import java.time.LocalDateTime;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public record PedidoStatusAlteradoEvent(
        Long pedidoId,
        Long clienteId,
        StatusPedido statusAnterior,
        StatusPedido statusNovo,
        LocalDateTime dataHora
) {
}
