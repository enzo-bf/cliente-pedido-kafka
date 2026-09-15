package com.enzobf.cliente_pedido_kafka.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.PedidoNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.messaging.PedidoEventProducer;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.HistoricoPedidoRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final String EVENTO_PEDIDO_PROCESSADO = "PEDIDO_PROCESSADO";

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoPedidoRepository historicoPedidoRepository;
    private final CalculadoraDesconto calculadoraDesconto;
    private final PedidoEventProducer pedidoEventProducer;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            HistoricoPedidoRepository historicoPedidoRepository,
            CalculadoraDesconto calculadoraDesconto,
            PedidoEventProducer pedidoEventProducer
    ) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.historicoPedidoRepository = historicoPedidoRepository;
        this.calculadoraDesconto = calculadoraDesconto;
        this.pedidoEventProducer = pedidoEventProducer;
    }

    public PedidoResponse cadastrar(PedidoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(request.clienteId()));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setDescricao(request.descricao());
        pedido.setValor(request.valor());
        pedido.setDataCriacao(LocalDateTime.now());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        pedidoEventProducer.publicarPedidoCriado(new PedidoCriadoEvent(
                pedidoSalvo.getId(),
                cliente.getId(),
                pedidoSalvo.getValor(),
                pedidoSalvo.getDataCriacao()
        ));

        return converterParaResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNaoEncontradoException(clienteId);
        }

        return pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoricoPedidoResponse> listarHistorico(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new PedidoNaoEncontradoException(pedidoId);
        }

        return historicoPedidoRepository
                .findByPedidoIdOrderByDataProcessamentoAsc(pedidoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional
    public void processarPedidoCriado(PedidoCriadoEvent evento) {
        Pedido pedido = buscarEntidade(evento.pedidoId());

        if (pedido.getValorFinal() != null) {
            log.info("Pedido {} já processado; evento ignorado", pedido.getId());
            return;
        }

        BigDecimal desconto = calculadoraDesconto.calcular(pedido.getValor());
        pedido.setDesconto(desconto);
        pedido.setValorFinal(pedido.getValor().subtract(desconto));

        pedidoRepository.save(pedido);
        registrarHistorico(pedido);
    }

    private void registrarHistorico(Pedido pedido) {
        HistoricoPedido historico = new HistoricoPedido();
        historico.setPedidoId(pedido.getId());
        historico.setTipoEvento(EVENTO_PEDIDO_PROCESSADO);
        historico.setDataProcessamento(LocalDateTime.now());
        historico.setDescricao(
                "Desconto de " + pedido.getDesconto()
                        + " aplicado; valor final " + pedido.getValorFinal()
        );

        historicoPedidoRepository.save(historico);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    private PedidoResponse converterParaResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getCliente() == null ? null : pedido.getCliente().getId(),
                pedido.getDescricao(),
                pedido.getValor(),
                pedido.getDesconto(),
                pedido.getValorFinal(),
                pedido.getDataCriacao()
        );
    }

    private HistoricoPedidoResponse converterParaResponse(HistoricoPedido historico) {
        return new HistoricoPedidoResponse(
                historico.getId(),
                historico.getPedidoId(),
                historico.getTipoEvento(),
                historico.getDataProcessamento(),
                historico.getDescricao()
        );
    }
}
