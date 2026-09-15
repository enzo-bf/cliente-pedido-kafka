package com.enzobf.cliente_pedido_kafka.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;
import com.enzobf.cliente_pedido_kafka.kafka.consumer.PedidoEventConsumer;

class PedidoEventConsumerTest {

    private final PedidoEventConsumer consumer = new PedidoEventConsumer();

    @Test
    void deveConsumirEventosSemErro() {
        consumer.consumirPedidoCriado(new PedidoCriadoEvent(
                1L,
                2L,
                "Pedido teste",
                new BigDecimal("100.00"),
                StatusPedido.CRIADO,
                LocalDateTime.now()
        ));
        consumer.consumirStatusAlterado(new PedidoStatusAlteradoEvent(
                1L,
                2L,
                StatusPedido.CRIADO,
                StatusPedido.PROCESSANDO,
                LocalDateTime.now()
        ));
    }
}
