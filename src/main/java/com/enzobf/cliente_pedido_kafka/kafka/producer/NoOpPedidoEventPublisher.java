package com.enzobf.cliente_pedido_kafka.kafka.producer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("test")
public class NoOpPedidoEventPublisher implements PedidoEventPublisher {

    @Override
    public void publicarPedidoCriado(PedidoCriadoEvent evento) {
        log.debug("NoOp PedidoCriadoEvent pedidoId={}", evento.pedidoId());
    }

    @Override
    public void publicarStatusAlterado(PedidoStatusAlteradoEvent evento) {
        log.debug("NoOp PedidoStatusAlteradoEvent pedidoId={}", evento.pedidoId());
    }
}
