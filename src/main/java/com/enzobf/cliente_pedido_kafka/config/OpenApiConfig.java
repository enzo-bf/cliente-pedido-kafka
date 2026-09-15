package com.enzobf.cliente_pedido_kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cliente Pedido Kafka API")
                        .version("1.0.0")
                        .description("API corporativa para gestão de clientes, pedidos, histórico e eventos Kafka")
                        .contact(new Contact()
                                .name("Enzo")
                                .email("enzo@cliente-pedido-kafka.local")));
    }
}
