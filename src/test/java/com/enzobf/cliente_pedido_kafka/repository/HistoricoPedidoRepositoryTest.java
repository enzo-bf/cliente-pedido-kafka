package com.enzobf.cliente_pedido_kafka.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;

/**
 * Testa a busca paginada de histórico por tipo de evento
 * (GET /historico-pedidos?tipoEvento=...) contra um banco H2 real.
 *
 * <p>O {@code @BeforeEach} limpa a tabela antes de cada teste porque o H2
 * de teste ({@code jdbc:h2:mem:testdb}) é compartilhado por toda a suíte, e
 * o {@link com.enzobf.cliente_pedido_kafka.messaging.PedidoEventIntegrationTest}
 * grava histórico de forma assíncrona (consumer Kafka em outra thread), fora
 * da transação desta classe — portanto não é revertido pelo
 * {@code @Transactional} e poderia poluir a contagem esperada aqui.</p>
 */
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"
})
@Transactional
class HistoricoPedidoRepositoryTest {

    @Autowired
    private HistoricoPedidoRepository historicoPedidoRepository;

    @BeforeEach
    void limparHistorico() {
        historicoPedidoRepository.deleteAll();
    }

    private void salvar(Long pedidoId, String tipoEvento) {
        HistoricoPedido historico = new HistoricoPedido();
        historico.setPedidoId(pedidoId);
        historico.setTipoEvento(tipoEvento);
        historico.setDataProcessamento(LocalDateTime.now());
        historico.setDescricao("desc");
        historicoPedidoRepository.save(historico);
    }

    @Test
    void deveFiltrarHistoricoPorTipoEventoPaginado() {
        salvar(1L, "PEDIDO_PROCESSADO");
        salvar(2L, "PEDIDO_PROCESSADO");
        salvar(3L, "PEDIDO_ATUALIZADO");

        Page<HistoricoPedido> pagina = historicoPedidoRepository.findByTipoEvento(
                "PEDIDO_PROCESSADO", PageRequest.of(0, 10));

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).allMatch(h -> h.getTipoEvento().equals("PEDIDO_PROCESSADO"));
    }

    @Test
    void deveRetornarPaginaVaziaParaTipoEventoInexistente() {
        salvar(1L, "PEDIDO_PROCESSADO");

        Page<HistoricoPedido> pagina = historicoPedidoRepository.findByTipoEvento(
                "TIPO_INEXISTENTE", PageRequest.of(0, 10));

        assertThat(pagina.getContent()).isEmpty();
    }
}
