package com.enzobf.cliente_pedido_kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topic")
public record KafkaTopicProperties(
        String pedidosCriados,
        int particoes,
        short replicas
) {
}
