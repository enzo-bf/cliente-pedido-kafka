package com.enzobf.cliente_pedido_kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoCriadoEvent(
        Long pedidoId,
        Long clienteId,
        BigDecimal valor,
        LocalDateTime dataCriacao
) {
}
