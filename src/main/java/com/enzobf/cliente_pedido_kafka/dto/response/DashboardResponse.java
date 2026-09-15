package com.enzobf.cliente_pedido_kafka.dto.response;

import java.util.Map;

public record DashboardResponse(
        long totalClientes,
        long totalPedidos,
        Map<String, Long> pedidosPorStatus
) {
}
