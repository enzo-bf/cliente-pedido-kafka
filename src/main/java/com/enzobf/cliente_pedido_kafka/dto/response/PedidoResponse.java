package com.enzobf.cliente_pedido_kafka.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        BigDecimal desconto,
        BigDecimal valorFinal,
        LocalDateTime dataCriacao,
        Long clienteId,
        String clienteNome
) {
}
