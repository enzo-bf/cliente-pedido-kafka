 package com.enzobf.cliente_pedido_kafka.kafka.consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test")
public class PedidoEventConsumer {

    @KafkaListener(topics = "${app.kafka.topics.pedido-criado}", groupId = "cliente-pedido-kafka")
    public void consumirPedidoCriado(PedidoCriadoEvent evento) {
        log.info(
                "event=PedidoCriadoEvent pedidoId={} clienteId={} valorFinal={} status={} dataCriacao={}",
                evento.pedidoId(),
                evento.clienteId(),
                evento.valorFinal(),
                evento.status(),
                evento.dataCriacao()
        );
    }

    @KafkaListener(topics = "${app.kafka.topics.pedido-status-alterado}", groupId = "cliente-pedido-kafka")
    public void consumirStatusAlterado(PedidoStatusAlteradoEvent evento) {
        log.info(
                "event=PedidoStatusAlteradoEvent pedidoId={} clienteId={} statusAnterior={} statusNovo={} dataHora={}",
                evento.pedidoId(),
                evento.clienteId(),
                evento.statusAnterior(),
                evento.statusNovo(),
                evento.dataHora()
        );
    }
}
