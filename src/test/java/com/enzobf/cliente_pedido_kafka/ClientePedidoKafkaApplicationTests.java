package com.enzobf.cliente_pedido_kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.kafka.listener.auto-startup=false",
		"spring.kafka.admin.auto-create=false"
})
class ClientePedidoKafkaApplicationTests {

	@Test
	void contextLoads() {
	}

}
