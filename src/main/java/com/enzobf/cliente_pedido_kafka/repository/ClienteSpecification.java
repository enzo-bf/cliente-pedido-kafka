package com.enzobf.cliente_pedido_kafka.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;

import jakarta.persistence.criteria.Predicate;

/**
 * Especificações JPA para filtros dinâmicos e combináveis na listagem de
 * clientes (GET /clientes).
 */
public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Cliente> comFiltros(String cpf, String nome) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cpf != null && !cpf.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("cpf"), cpf));
            }

            if (nome != null && !nome.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
