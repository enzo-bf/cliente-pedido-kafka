package com.enzobf.cliente_pedido_kafka.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    boolean existsByClienteId(Long clienteId);

    long countByStatus(StatusPedido status);

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<Pedido> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "cliente")
    Page<Pedido> findAll(Specification<Pedido> spec, Pageable pageable);
}
