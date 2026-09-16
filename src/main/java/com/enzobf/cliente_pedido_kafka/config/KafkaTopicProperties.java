package com.enzobf.cliente_pedido_kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.kafka.topic")
public record KafkaTopicProperties(
        String pedidosCriados,
        @DefaultValue("pedidos.atualizados") String pedidosAtualizados,
        int particoes,
        short replicas
) {
}
