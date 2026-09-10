package com.enzobf.cliente_pedido_kafka.service;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.DescontoInvalidoException;
import com.enzobf.cliente_pedido_kafka.exception.ValorInvalidoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PedidoService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal DESCONTO_MAXIMO = new BigDecimal("20");

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    public PedidoResponse cadastrar(PedidoRequest request) {
        validarValor(request.valor());
        validarDesconto(request.desconto());

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() ->
                        new ClienteNaoEncontradoException(request.clienteId())
                );

        BigDecimal valorFinal = calcularValorFinal(
                request.valor(),
                request.desconto()
        );

        Pedido pedido = new Pedido();
        pedido.setDescricao(request.descricao());
        pedido.setValor(request.valor());
        pedido.setDesconto(request.desconto());
        pedido.setValorFinal(valorFinal);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setCliente(cliente);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return converterParaResponse(pedidoSalvo);
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException();
        }
    }

    private void validarDesconto(BigDecimal desconto) {
        if (desconto == null
                || desconto.compareTo(BigDecimal.ZERO) < 0
                || desconto.compareTo(DESCONTO_MAXIMO) > 0) {
            throw new DescontoInvalidoException();
        }
    }

    private BigDecimal calcularValorFinal(
            BigDecimal valor,
            BigDecimal desconto
    ) {
        BigDecimal valorDoDesconto = valor
                .multiply(desconto)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        BigDecimal valorFinal = valor.subtract(valorDoDesconto);

        if (valorFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException();
        }

        return valorFinal.setScale(2, RoundingMode.HALF_UP);
    }

    private PedidoResponse converterParaResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getDescricao(),
                pedido.getValor(),
                pedido.getDesconto(),
                pedido.getValorFinal(),
                pedido.getDataCriacao(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome()
        );
    }
}