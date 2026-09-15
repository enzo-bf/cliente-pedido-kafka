package com.enzobf.cliente_pedido_kafka.kafka.producer;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;

public interface PedidoEventPublisher {

    void publicarPedidoCriado(PedidoCriadoEvent evento);

    void publicarStatusAlterado(PedidoStatusAlteradoEvent evento);
}
