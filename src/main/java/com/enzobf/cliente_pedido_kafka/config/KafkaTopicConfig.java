package com.enzobf.cliente_pedido_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    @Bean
    public NewTopic pedidosCriadosTopic(KafkaTopicProperties properties) {
        return TopicBuilder
                .name(properties.pedidosCriados())
                .partitions(properties.particoes())
                .replicas(properties.replicas())
                .build();
    }

    @Bean
    public NewTopic pedidosAtualizadosTopic(KafkaTopicProperties properties) {
        return TopicBuilder
                .name(properties.pedidosAtualizados())
                .partitions(properties.particoes())
                .replicas(properties.replicas())
                .build();
    }
}
