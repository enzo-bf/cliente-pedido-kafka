package com.enzobf.cliente_pedido_kafka.dto.request;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

import jakarta.validation.constraints.NotNull;

public record StatusPedidoRequest(

        @NotNull(message = "O novo status é obrigatório")
        StatusPedido status

) {
}
