package com.enzobf.cliente_pedido_kafka.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PedidoRequest(

        @NotBlank(message = "A descrição do pedido é obrigatória")
        String descricao,

        @NotNull(message = "O valor do pedido é obrigatório")
        @Positive(message = "O valor do pedido deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O desconto é obrigatório")
        @DecimalMin(
                value = "0.0",
                message = "O desconto não pode ser negativo"
        )
        @DecimalMax(
                value = "20.0",
                message = "O desconto não pode ser superior a 20%"
        )
        BigDecimal desconto,

        @NotNull(message = "O cliente é obrigatório")
        Long clienteId

) {
}
