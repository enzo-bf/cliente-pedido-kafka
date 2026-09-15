package com.enzobf.cliente_pedido_kafka.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.request.StatusPedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Cadastrar pedido e publicar evento Kafka")
    public ResponseEntity<PedidoResponse> cadastrar(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.cadastrar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar pedidos com filtros")
    public ResponseEntity<Page<PedidoResponse>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) String descricao,
            Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listar(clienteId, status, descricao, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do pedido")
    public ResponseEntity<PedidoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status do pedido")
    public ResponseEntity<PedidoResponse> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusPedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.alterarStatus(id, request.status()));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Consultar histórico de status do pedido")
    public ResponseEntity<List<HistoricoPedidoResponse>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.listarHistorico(id));
    }
}
