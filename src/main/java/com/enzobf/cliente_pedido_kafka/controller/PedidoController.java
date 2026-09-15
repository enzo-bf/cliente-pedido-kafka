package com.enzobf.cliente_pedido_kafka.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> cadastrar(
            @Valid @RequestBody PedidoRequest request
    ) {
        PedidoResponse response = pedidoService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPorCliente(
            @RequestParam Long clienteId
    ) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoPedidoResponse>> listarHistorico(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(pedidoService.listarHistorico(id));
    }
}
