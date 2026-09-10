package com.enzobf.cliente_pedido_kafka.controller;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse response = clienteService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable Long id
    ) {
        ClienteResponse response = clienteService.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(Pageable pageable) {
        Page<ClienteResponse> clientes = clienteService.listar(pageable);

        return ResponseEntity.ok(clientes);
    }
}