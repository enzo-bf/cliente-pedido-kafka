package com.enzobf.cliente_pedido_kafka.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
public class PedidoEventProducer implements PedidoEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicPedidoCriado;
    private final String topicPedidoStatusAlterado;

    public PedidoEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.pedido-criado}") String topicPedidoCriado,
            @Value("${app.kafka.topics.pedido-status-alterado}") String topicPedidoStatusAlterado
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicPedidoCriado = topicPedidoCriado;
        this.topicPedidoStatusAlterado = topicPedidoStatusAlterado;
    }

    @Override
    public void publicarPedidoCriado(PedidoCriadoEvent evento) {
        log.info(
                "Publicando PedidoCriadoEvent pedidoId={} clienteId={} status={}",
                evento.pedidoId(),
                evento.clienteId(),
                evento.status()
        );
        kafkaTemplate.send(topicPedidoCriado, String.valueOf(evento.pedidoId()), evento);
    }

    @Override
    public void publicarStatusAlterado(PedidoStatusAlteradoEvent evento) {
        log.info(
                "Publicando PedidoStatusAlteradoEvent pedidoId={} de={} para={}",
                evento.pedidoId(),
                evento.statusAnterior(),
                evento.statusNovo()
        );
        kafkaTemplate.send(topicPedidoStatusAlterado, String.valueOf(evento.pedidoId()), evento);
    }
}
