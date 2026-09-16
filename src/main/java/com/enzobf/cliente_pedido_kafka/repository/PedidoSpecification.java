package com.enzobf.cliente_pedido_kafka.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.enzobf.cliente_pedido_kafka.entity.Pedido;

import jakarta.persistence.criteria.Predicate;

/**
 * Especificações JPA para filtros dinâmicos e combináveis na listagem de
 * pedidos (GET /pedidos): por cliente e por faixa de valor.
 */
public final class PedidoSpecification {

    private PedidoSpecification() {
    }

    public static Specification<Pedido> comFiltros(Long clienteId, BigDecimal valorMin, BigDecimal valorMax) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (clienteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), clienteId));
            }

            if (valorMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("valor"), valorMin));
            }

            if (valorMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("valor"), valorMax));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
