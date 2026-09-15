package com.enzobf.cliente_pedido_kafka.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(topics = "pedidos.criados", partitions = 1)
class PedidoEventIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void deveProcessarPedidoPublicadoNoKafkaEGerarHistorico() {
        Cliente cliente = clienteRepository.save(new Cliente(
                null, "Enzo", "98765432100", "enzo@email.com", LocalDateTime.now()));

        PedidoResponse pedido = pedidoService.cadastrar(new PedidoRequest(
                cliente.getId(), "Notebook", new BigDecimal("1000.00")));

        assertThat(pedido.valorFinal()).isNull();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            PedidoResponse processado = pedidoService.buscarPorId(pedido.id());

            assertThat(processado.desconto()).isEqualByComparingTo("100.00");
            assertThat(processado.valorFinal()).isEqualByComparingTo("900.00");
            assertThat(pedidoService.listarHistorico(pedido.id()))
                    .singleElement()
                    .satisfies(historico ->
                            assertThat(historico.tipoEvento()).isEqualTo("PEDIDO_PROCESSADO"));
        });
    }
}
