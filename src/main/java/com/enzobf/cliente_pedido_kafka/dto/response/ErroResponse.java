package com.enzobf.cliente_pedido_kafka.dto.response;

import java.time.LocalDateTime;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
}