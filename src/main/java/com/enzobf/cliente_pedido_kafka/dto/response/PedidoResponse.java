package com.enzobf.cliente_pedido_kafka.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public record PedidoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        BigDecimal desconto,
        BigDecimal valorFinal,
        LocalDateTime dataCriacao,
        StatusPedido status,
        Long clienteId,
        String clienteNome
) {
}
