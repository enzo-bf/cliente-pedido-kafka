package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
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

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HistoricoPedidoRepository historicoPedidoRepository;

    @Mock
    private PedidoEventProducer pedidoEventProducer;

    @Spy
    private CalculadoraDesconto calculadoraDesconto = new CalculadoraDesconto();

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente() {
        return new Cliente(1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());
    }

    private Pedido pedido(BigDecimal valor) {
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setCliente(cliente());
        pedido.setDescricao("Notebook");
        pedido.setValor(valor);
        pedido.setDataCriacao(LocalDateTime.now());
        return pedido;
    }

    @Test
    void deveCadastrarPedidoEPublicarEvento() {
        PedidoRequest request = new PedidoRequest(1L, "Notebook", new BigDecimal("1000.00"));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente()));
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> {
                    Pedido pedido = invocation.getArgument(0);
                    pedido.setId(10L);
                    return pedido;
                });

        PedidoResponse response = pedidoService.cadastrar(request);

        ArgumentCaptor<PedidoCriadoEvent> captor = ArgumentCaptor.forClass(PedidoCriadoEvent.class);
        verify(pedidoEventProducer).publicarPedidoCriado(captor.capture());

        assertThat(captor.getValue().pedidoId()).isEqualTo(10L);
        assertThat(captor.getValue().clienteId()).isEqualTo(1L);
        assertThat(response.valorFinal()).isNull();
        assertThat(response.clienteId()).isEqualTo(1L);
    }

    @Test
    void naoDeveCadastrarPedidoParaClienteInexistente() {
        PedidoRequest request = new PedidoRequest(99L, "Notebook", new BigDecimal("10.00"));

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.cadastrar(request))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verify(pedidoRepository, never()).save(any());
        verify(pedidoEventProducer, never()).publicarPedidoCriado(any());
    }

    @Test
    void deveAplicarDescontoERegistrarHistoricoAoProcessarEvento() {
        Pedido pedido = pedido(new BigDecimal("1000.00"));

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        pedidoService.processarPedidoCriado(new PedidoCriadoEvent(
                10L, 1L, pedido.getValor(), pedido.getDataCriacao()));

        assertThat(pedido.getDesconto()).isEqualByComparingTo("100.00");
        assertThat(pedido.getValorFinal()).isEqualByComparingTo("900.00");

        ArgumentCaptor<HistoricoPedido> captor = ArgumentCaptor.forClass(HistoricoPedido.class);
        verify(historicoPedidoRepository).save(captor.capture());

        assertThat(captor.getValue().getPedidoId()).isEqualTo(10L);
        assertThat(captor.getValue().getTipoEvento()).isEqualTo("PEDIDO_PROCESSADO");
        assertThat(captor.getValue().getDataProcessamento()).isNotNull();
    }

    @Test
    void deveIgnorarEventoDePedidoJaProcessado() {
        Pedido pedido = pedido(new BigDecimal("1000.00"));
        pedido.setDesconto(new BigDecimal("100.00"));
        pedido.setValorFinal(new BigDecimal("900.00"));

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        pedidoService.processarPedidoCriado(new PedidoCriadoEvent(
                10L, 1L, pedido.getValor(), pedido.getDataCriacao()));

        verify(pedidoRepository, never()).save(any());
        verify(historicoPedidoRepository, never()).save(any());
    }

    @Test
    void deveListarPedidosDoCliente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(pedido(new BigDecimal("100.00"))));

        assertThat(pedidoService.listarPorCliente(1L)).hasSize(1);
    }

    @Test
    void deveFalharAoListarHistoricoDePedidoInexistente() {
        when(pedidoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.listarHistorico(99L))
                .isInstanceOf(PedidoNaoEncontradoException.class);
    }
}
