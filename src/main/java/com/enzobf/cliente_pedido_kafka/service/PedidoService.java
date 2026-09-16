package com.enzobf.cliente_pedido_kafka.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.HistoricoPedido;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.event.PedidoAtualizadoEvent;
import com.enzobf.cliente_pedido_kafka.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.PedidoNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.messaging.PedidoEventProducer;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.HistoricoPedidoRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoSpecification;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private static final String EVENTO_PEDIDO_PROCESSADO = "PEDIDO_PROCESSADO";
    private static final String EVENTO_PEDIDO_ATUALIZADO = "PEDIDO_ATUALIZADO";

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

    /**
     * Consulta simples e não paginada dos pedidos de um cliente. Mantida
     * por compatibilidade com o uso já existente; para a listagem geral
     * com paginação, ordenação e filtros combináveis, veja {@link #listar}.
     */
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

    /**
     * Lista pedidos de forma paginada, permitindo combinar filtros
     * opcionais por cliente e por faixa de valor (mínimo e/ou máximo). A
     * ordenação e a paginação vêm do {@link Pageable} recebido do
     * controller (parâmetros {@code page}, {@code size} e {@code sort}).
     */
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(Long clienteId, BigDecimal valorMin, BigDecimal valorMax, Pageable pageable) {
        Specification<Pedido> filtros = PedidoSpecification.comFiltros(clienteId, valorMin, valorMax);

        return pedidoRepository.findAll(filtros, pageable)
                .map(this::converterParaResponse);
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

    /**
     * Lista o histórico de processamento de pedidos de forma transversal,
     * com filtro opcional por tipo de evento (ex.: {@code PEDIDO_PROCESSADO},
     * {@code PEDIDO_ATUALIZADO}) e suporte a paginação/ordenação.
     */
    @Transactional(readOnly = true)
    public Page<HistoricoPedidoResponse> listarHistoricoPorTipoEvento(String tipoEvento, Pageable pageable) {
        Page<HistoricoPedido> historico = (tipoEvento == null || tipoEvento.isBlank())
                ? historicoPedidoRepository.findAll(pageable)
                : historicoPedidoRepository.findByTipoEvento(tipoEvento, pageable);

        return historico.map(this::converterParaResponse);
    }

    /**
     * Atualiza os dados do pedido de forma síncrona e publica o evento
     * Kafka {@code PEDIDO_ATUALIZADO}. Assim como no cadastro, o desconto e
     * o valor final são zerados e recalculados de forma assíncrona pelo
     * consumidor do evento, garantindo que refletem sempre o valor mais
     * recente do pedido.
     */
    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarEntidade(id);

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(request.clienteId()));

        pedido.setCliente(cliente);
        pedido.setDescricao(request.descricao());
        pedido.setValor(request.valor());
        pedido.setDesconto(null);
        pedido.setValorFinal(null);

        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        pedidoEventProducer.publicarPedidoAtualizado(new PedidoAtualizadoEvent(
                pedidoAtualizado.getId(),
                cliente.getId(),
                pedidoAtualizado.getDescricao(),
                pedidoAtualizado.getValor(),
                LocalDateTime.now()
        ));

        return converterParaResponse(pedidoAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscarEntidade(id);

        pedidoRepository.delete(pedido);
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
        registrarHistorico(pedido, EVENTO_PEDIDO_PROCESSADO);
    }

    /**
     * Recalcula o desconto após uma atualização de pedido e registra o
     * histórico {@code PEDIDO_ATUALIZADO}. Idempotente: se o pedido já
     * possui {@code valorFinal} (ou seja, o evento já foi processado ou o
     * pedido não foi resetado por uma atualização), o evento é ignorado.
     */
    @Transactional
    public void processarPedidoAtualizado(PedidoAtualizadoEvent evento) {
        Pedido pedido = buscarEntidade(evento.pedidoId());

        if (pedido.getValorFinal() != null) {
            log.info("Atualização do pedido {} já processada; evento ignorado", pedido.getId());
            return;
        }

        BigDecimal desconto = calculadoraDesconto.calcular(pedido.getValor());
        pedido.setDesconto(desconto);
        pedido.setValorFinal(pedido.getValor().subtract(desconto));

        pedidoRepository.save(pedido);
        registrarHistorico(pedido, EVENTO_PEDIDO_ATUALIZADO);
    }

    private void registrarHistorico(Pedido pedido, String tipoEvento) {
        HistoricoPedido historico = new HistoricoPedido();
        historico.setPedidoId(pedido.getId());
        historico.setTipoEvento(tipoEvento);
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
