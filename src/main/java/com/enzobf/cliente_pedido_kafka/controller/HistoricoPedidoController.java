package com.enzobf.cliente_pedido_kafka.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

import lombok.RequiredArgsConstructor;

/**
 * Consulta o histórico de processamento de pedidos de forma transversal
 * (não restrita a um único pedido), com filtro opcional por tipo de evento
 * e suporte a paginação/ordenação.
 */
@RestController
@RequestMapping("/historico-pedidos")
@RequiredArgsConstructor
public class HistoricoPedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<Page<HistoricoPedidoResponse>> listarPorTipoEvento(
            @RequestParam(required = false) String tipoEvento,
            @PageableDefault(size = 10, sort = "dataProcessamento", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listarHistoricoPorTipoEvento(tipoEvento, pageable));
    }
}
