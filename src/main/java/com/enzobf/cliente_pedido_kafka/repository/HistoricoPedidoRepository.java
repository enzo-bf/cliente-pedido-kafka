
package com.enzobf.cliente_pedido_kafka.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;

public interface HistoricoPedidoRepository
        extends JpaRepository<HistoricoPedido, Long> {

    List<HistoricoPedido> findByPedidoIdOrderByDataProcessamentoAsc(Long pedidoId);

    Page<HistoricoPedido> findByTipoEvento(String tipoEvento, Pageable pageable);
}
