package com.enzobf.cliente_pedido_kafka.mapper;

import org.springframework.stereotype.Component;

import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;

@Component
public class PedidoMapper {

    public PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getDescricao(),
                pedido.getValor(),
                pedido.getDesconto(),
                pedido.getValorFinal(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome()
        );
    }

    public HistoricoPedidoResponse toHistoricoResponse(HistoricoPedido historico) {
        return new HistoricoPedidoResponse(
                historico.getId(),
                historico.getPedido().getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getDataHora()
        );
    }
}
