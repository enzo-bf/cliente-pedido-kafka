package com.enzobf.cliente_pedido_kafka.strategy;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

@Component
public class TransicaoStatusPedidoStrategy implements StatusPedidoStrategy {

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES = Map.of(
            StatusPedido.CRIADO, Set.of(StatusPedido.PROCESSANDO, StatusPedido.CANCELADO),
            StatusPedido.PROCESSANDO, Set.of(StatusPedido.APROVADO, StatusPedido.REJEITADO, StatusPedido.CANCELADO),
            StatusPedido.APROVADO, Set.of(StatusPedido.FINALIZADO, StatusPedido.CANCELADO),
            StatusPedido.REJEITADO, Set.of(StatusPedido.CANCELADO),
            StatusPedido.CANCELADO, Set.of(),
            StatusPedido.FINALIZADO, Set.of()
    );

    @Override
    public boolean podeTransicionar(StatusPedido atual, StatusPedido novo) {
        if (atual == null || novo == null || atual == novo) {
            return false;
        }
        return TRANSICOES.getOrDefault(atual, Set.of()).contains(novo);
    }
}
