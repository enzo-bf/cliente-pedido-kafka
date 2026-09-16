package com.enzobf.cliente_pedido_kafka.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.enzobf.cliente_pedido_kafka.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);
}
