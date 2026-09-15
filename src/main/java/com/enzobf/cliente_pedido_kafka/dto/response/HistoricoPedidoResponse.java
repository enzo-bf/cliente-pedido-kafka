package com.enzobf.cliente_pedido_kafka.dto.response;

import java.time.LocalDateTime;

public record HistoricoPedidoResponse(
        Long id,
        Long pedidoId,
        String tipoEvento,
        LocalDateTime dataProcessamento,
        String descricao
) {
}
