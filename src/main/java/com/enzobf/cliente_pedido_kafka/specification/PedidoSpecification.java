package com.enzobf.cliente_pedido_kafka.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public final class PedidoSpecification {

    private PedidoSpecification() {
    }

    public static Specification<Pedido> comFiltros(Long clienteId, StatusPedido status, String descricao) {
        return Specification
                .where(porCliente(clienteId))
                .and(porStatus(status))
                .and(porDescricao(descricao));
    }

    private static Specification<Pedido> porCliente(Long clienteId) {
        return (root, query, builder) -> {
            if (clienteId == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("cliente").get("id"), clienteId);
        };
    }

    private static Specification<Pedido> porStatus(StatusPedido status) {
        return (root, query, builder) -> {
            if (status == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("status"), status);
        };
    }

    private static Specification<Pedido> porDescricao(String descricao) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(descricao)) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("descricao")), "%" + descricao.toLowerCase() + "%");
        };
    }
}
