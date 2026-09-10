package com.enzobf.cliente_pedido_kafka.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    Page<Cliente> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );
}
