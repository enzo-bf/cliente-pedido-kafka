package com.enzobf.cliente_pedido_kafka.dto.response;

import java.time.LocalDateTime;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public record HistoricoPedidoResponse(
        Long id,
        Long pedidoId,
        StatusPedido statusAnterior,
        StatusPedido statusNovo,
        LocalDateTime dataHora
) {
}
