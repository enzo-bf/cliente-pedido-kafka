package com.enzobf.cliente_pedido_kafka.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;

/**
 * Testa os filtros combináveis (cliente, faixa de valor) e a paginação da
 * listagem de pedidos contra um banco H2 real, exercitando a
 * {@link PedidoSpecification} de ponta a ponta via {@link PedidoRepository}.
 */
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"
})
@Transactional
class PedidoRepositorySpecificationTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Cliente clienteSalvo(String cpf) {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente " + cpf);
        cliente.setCpf(cpf);
        cliente.setEmail(cpf + "@email.com");
        cliente.setDataCadastro(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    private Pedido pedidoSalvo(Cliente cliente, BigDecimal valor) {
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDescricao("Pedido de " + valor);
        pedido.setValor(valor);
        pedido.setDataCriacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Test
    void deveFiltrarPorCliente() {
        Cliente cliente1 = clienteSalvo("11111111111");
        Cliente cliente2 = clienteSalvo("22222222222");
        pedidoSalvo(cliente1, new BigDecimal("100.00"));
        pedidoSalvo(cliente2, new BigDecimal("200.00"));

        Page<Pedido> pagina = pedidoRepository.findAll(
                PedidoSpecification.comFiltros(cliente1.getId(), null, null),
                PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getCliente().getId()).isEqualTo(cliente1.getId());
    }

    @Test
    void deveFiltrarPorFaixaDeValorCombinada() {
        Cliente cliente = clienteSalvo("33333333333");
        pedidoSalvo(cliente, new BigDecimal("50.00"));
        pedidoSalvo(cliente, new BigDecimal("300.00"));
        pedidoSalvo(cliente, new BigDecimal("800.00"));

        Page<Pedido> pagina = pedidoRepository.findAll(
                PedidoSpecification.comFiltros(null, new BigDecimal("100.00"), new BigDecimal("500.00")),
                PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getValor()).isEqualByComparingTo("300.00");
    }

    @Test
    void devePaginarResultados() {
        Cliente cliente = clienteSalvo("44444444444");
        pedidoSalvo(cliente, new BigDecimal("10.00"));
        pedidoSalvo(cliente, new BigDecimal("20.00"));
        pedidoSalvo(cliente, new BigDecimal("30.00"));

        Page<Pedido> pagina = pedidoRepository.findAll(
                PedidoSpecification.comFiltros(cliente.getId(), null, null),
                PageRequest.of(0, 2));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }
}
