package com.enzobf.cliente_pedido_kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mensagem publicada no tópico de atualização de pedidos. Segue o mesmo
 * padrão de {@link PedidoCriadoEvent}: a atualização é persistida de forma
 * síncrona pelo {@code PedidoService#atualizar}, e o recálculo do desconto
 * e o registro do histórico "PEDIDO_ATUALIZADO" acontecem de forma
 * assíncrona no consumidor deste evento.
 */
public record PedidoAtualizadoEvent(
        Long pedidoId,
        Long clienteId,
        String descricao,
        BigDecimal valor,
        LocalDateTime dataAtualizacao
) {
}
