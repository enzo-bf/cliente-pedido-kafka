package com.enzobf.cliente_pedido_kafka.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.event.PedidoAtualizadoEvent;
import com.enzobf.cliente_pedido_kafka.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

@Component
public class PedidoEventConsumer {

    private final PedidoService pedidoService;

    public PedidoEventConsumer(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.pedidos-criados}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumirPedidoCriado(PedidoCriadoEvent evento) {
        pedidoService.processarPedidoCriado(evento);
    }

    @KafkaListener(
            topics = "${app.kafka.topic.pedidos-atualizados}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumirPedidoAtualizado(PedidoAtualizadoEvent evento) {
        pedidoService.processarPedidoAtualizado(evento);
    }
}
