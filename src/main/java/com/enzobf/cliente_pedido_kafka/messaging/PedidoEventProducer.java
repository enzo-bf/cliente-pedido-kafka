package com.enzobf.cliente_pedido_kafka.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.config.KafkaTopicProperties;
import com.enzobf.cliente_pedido_kafka.event.PedidoAtualizadoEvent;
import com.enzobf.cliente_pedido_kafka.event.PedidoCriadoEvent;

@Component
public class PedidoEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public PedidoEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTopicProperties topicProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    public void publicarPedidoCriado(PedidoCriadoEvent evento) {
        kafkaTemplate.send(
                topicProperties.pedidosCriados(),
                String.valueOf(evento.pedidoId()),
                evento
        );
    }

    public void publicarPedidoAtualizado(PedidoAtualizadoEvent evento) {
        kafkaTemplate.send(
                topicProperties.pedidosAtualizados(),
                String.valueOf(evento.pedidoId()),
                evento
        );
    }
}
