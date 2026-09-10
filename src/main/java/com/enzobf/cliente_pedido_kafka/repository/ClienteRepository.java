package com.enzobf.cliente_pedido_kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);
}