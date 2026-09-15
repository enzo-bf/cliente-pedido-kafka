package com.enzobf.cliente_pedido_kafka.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public record PedidoCriadoEvent(
        Long pedidoId,
        Long clienteId,
        String descricao,
        BigDecimal valorFinal,
        StatusPedido status,
        LocalDateTime dataCriacao
) {
}
