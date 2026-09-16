package com.enzobf.cliente_pedido_kafka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados exibidos no topo do Swagger UI (título, versão, descrição e contato),
 * apenas para documentação — não altera nenhum contrato ou regra de negócio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clientePedidoKafkaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cliente Pedido Kafka API")
                        .description("API REST para cadastro de clientes e pedidos, com processamento "
                                + "assíncrono via Apache Kafka: o pedido é criado (ou atualizado) de forma "
                                + "síncrona, e o cálculo do desconto e o registro no histórico acontecem no "
                                + "consumidor do evento correspondente.")
                        .version("v1")
                        .contact(new Contact()
                                .name("GFT Brazil - Team 3"))
                        .license(new License()
                                .name("Uso interno - desafio técnico")));
    }
}
