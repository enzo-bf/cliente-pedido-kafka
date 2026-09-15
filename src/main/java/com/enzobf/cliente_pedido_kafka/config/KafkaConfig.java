package com.enzobf.cliente_pedido_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class KafkaConfig {

    @Bean
    public NewTopic pedidoCriadoTopic(
            @Value("${app.kafka.topics.pedido-criado}") String topic
    ) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic pedidoStatusAlteradoTopic(
            @Value("${app.kafka.topics.pedido-status-alterado}") String topic
    ) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
