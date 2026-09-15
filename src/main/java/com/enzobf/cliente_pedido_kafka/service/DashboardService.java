package com.enzobf.cliente_pedido_kafka.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.dto.response.DashboardResponse;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public DashboardResponse obterMetricas() {
        Map<String, Long> pedidosPorStatus = new LinkedHashMap<>();
        Arrays.stream(StatusPedido.values())
                .forEach(status -> pedidosPorStatus.put(status.name(), pedidoRepository.countByStatus(status)));

        return new DashboardResponse(
                clienteRepository.count(),
                pedidoRepository.count(),
                pedidosPorStatus
        );
    }
}
