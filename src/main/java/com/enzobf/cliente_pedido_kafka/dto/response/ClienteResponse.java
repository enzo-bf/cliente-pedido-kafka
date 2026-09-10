package com.enzobf.cliente_pedido_kafka.dto.response;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        LocalDateTime dataCadastro
) {
}
