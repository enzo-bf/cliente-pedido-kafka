package com.enzobf.cliente_pedido_kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enzobf.cliente_pedido_kafka.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
