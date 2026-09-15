package com.enzobf.cliente_pedido_kafka.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;
import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;
import com.enzobf.cliente_pedido_kafka.exception.DescontoInvalidoException;
import com.enzobf.cliente_pedido_kafka.exception.PedidoNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.StatusPedidoInvalidoException;
import com.enzobf.cliente_pedido_kafka.exception.ValorInvalidoException;
import com.enzobf.cliente_pedido_kafka.kafka.producer.PedidoEventPublisher;
import com.enzobf.cliente_pedido_kafka.mapper.PedidoMapper;
import com.enzobf.cliente_pedido_kafka.repository.HistoricoPedidoRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;
import com.enzobf.cliente_pedido_kafka.specification.PedidoSpecification;
import com.enzobf.cliente_pedido_kafka.strategy.StatusPedidoStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal DESCONTO_MAXIMO = new BigDecimal("20");

    private final PedidoRepository pedidoRepository;
    private final HistoricoPedidoRepository historicoPedidoRepository;
    private final ClienteService clienteService;
    private final PedidoMapper pedidoMapper;
    private final StatusPedidoStrategy statusPedidoStrategy;
    private final PedidoEventPublisher pedidoEventPublisher;

    @Transactional
    public PedidoResponse cadastrar(PedidoRequest request) {
        validarValor(request.valor());
        validarDesconto(request.desconto());

        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        BigDecimal valorFinal = calcularValorFinal(request.valor(), request.desconto());

        Pedido pedido = Pedido.builder()
                .descricao(request.descricao().trim())
                .valor(request.valor())
                .desconto(request.desconto())
                .valorFinal(valorFinal)
                .dataCriacao(LocalDateTime.now())
                .status(StatusPedido.CRIADO)
                .cliente(cliente)
                .build();

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        registrarHistorico(pedidoSalvo, null, StatusPedido.CRIADO);

        pedidoEventPublisher.publicarPedidoCriado(new PedidoCriadoEvent(
                pedidoSalvo.getId(),
                cliente.getId(),
                pedidoSalvo.getDescricao(),
                pedidoSalvo.getValorFinal(),
                pedidoSalvo.getStatus(),
                pedidoSalvo.getDataCriacao()
        ));

        log.info("Pedido criado id={} clienteId={}", pedidoSalvo.getId(), cliente.getId());
        return pedidoMapper.toResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return pedidoMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(Long clienteId, StatusPedido status, String descricao, Pageable pageable) {
        return pedidoRepository
                .findAll(PedidoSpecification.comFiltros(clienteId, status, descricao), pageable)
                .map(pedidoMapper::toResponse);
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarEntidade(id);
        validarValor(request.valor());
        validarDesconto(request.desconto());

        Cliente cliente = clienteService.buscarEntidade(request.clienteId());

        pedido.setDescricao(request.descricao().trim());
        pedido.setValor(request.valor());
        pedido.setDesconto(request.desconto());
        pedido.setValorFinal(calcularValorFinal(request.valor(), request.desconto()));
        pedido.setCliente(cliente);

        Pedido atualizado = pedidoRepository.save(pedido);
        log.info("Pedido atualizado id={}", atualizado.getId());
        return pedidoMapper.toResponse(atualizado);
    }

    @Transactional
    public PedidoResponse alterarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido statusAnterior = pedido.getStatus();

        if (!statusPedidoStrategy.podeTransicionar(statusAnterior, novoStatus)) {
            throw new StatusPedidoInvalidoException(statusAnterior, novoStatus);
        }

        pedido.setStatus(novoStatus);
        Pedido atualizado = pedidoRepository.save(pedido);
        registrarHistorico(atualizado, statusAnterior, novoStatus);

        pedidoEventPublisher.publicarStatusAlterado(new PedidoStatusAlteradoEvent(
                atualizado.getId(),
                atualizado.getCliente().getId(),
                statusAnterior,
                novoStatus,
                LocalDateTime.now()
        ));

        log.info("Status do pedido {} alterado de {} para {}", id, statusAnterior, novoStatus);
        return pedidoMapper.toResponse(atualizado);
    }

    @Transactional
    public PedidoResponse cancelar(Long id) {
        return alterarStatus(id, StatusPedido.CANCELADO);
    }

    @Transactional(readOnly = true)
    public List<HistoricoPedidoResponse> listarHistorico(Long pedidoId) {
        buscarEntidade(pedidoId);
        return historicoPedidoRepository.findByPedidoIdOrderByDataHoraAsc(pedidoId)
                .stream()
                .map(pedidoMapper::toHistoricoResponse)
                .toList();
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    private void registrarHistorico(Pedido pedido, StatusPedido anterior, StatusPedido novo) {
        HistoricoPedido historico = HistoricoPedido.builder()
                .pedido(pedido)
                .statusAnterior(anterior)
                .statusNovo(novo)
                .dataHora(LocalDateTime.now())
                .build();
        historicoPedidoRepository.save(historico);
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

    private BigDecimal calcularValorFinal(BigDecimal valor, BigDecimal desconto) {
        BigDecimal valorDoDesconto = valor
                .multiply(desconto)
                .divide(CEM, 2, RoundingMode.HALF_UP);

        BigDecimal valorFinal = valor.subtract(valorDoDesconto);
        if (valorFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException();
        }
        return valorFinal.setScale(2, RoundingMode.HALF_UP);
    }
}
