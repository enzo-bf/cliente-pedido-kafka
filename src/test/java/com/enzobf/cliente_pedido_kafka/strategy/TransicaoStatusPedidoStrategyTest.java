package com.enzobf.cliente_pedido_kafka.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

class TransicaoStatusPedidoStrategyTest {

    private final TransicaoStatusPedidoStrategy strategy = new TransicaoStatusPedidoStrategy();

    @Test
    void devePermitirFluxoPrincipal() {
        assertThat(strategy.podeTransicionar(StatusPedido.CRIADO, StatusPedido.PROCESSANDO)).isTrue();
        assertThat(strategy.podeTransicionar(StatusPedido.PROCESSANDO, StatusPedido.APROVADO)).isTrue();
        assertThat(strategy.podeTransicionar(StatusPedido.APROVADO, StatusPedido.FINALIZADO)).isTrue();
    }

    @Test
    void naoDevePermitirTransicaoDeStatusFinal() {
        assertThat(strategy.podeTransicionar(StatusPedido.CANCELADO, StatusPedido.CRIADO)).isFalse();
        assertThat(strategy.podeTransicionar(StatusPedido.FINALIZADO, StatusPedido.PROCESSANDO)).isFalse();
        assertThat(strategy.podeTransicionar(StatusPedido.CRIADO, StatusPedido.CRIADO)).isFalse();
    }
}
