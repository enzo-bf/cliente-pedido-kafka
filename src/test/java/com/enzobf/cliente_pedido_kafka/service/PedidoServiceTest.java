package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enzobf.cliente_pedido_kafka.dto.event.PedidoCriadoEvent;
import com.enzobf.cliente_pedido_kafka.dto.event.PedidoStatusAlteradoEvent;
import com.enzobf.cliente_pedido_kafka.dto.request.PedidoRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.entity.Pedido;
import com.enzobf.cliente_pedido_kafka.enums.StatusPedido;
import com.enzobf.cliente_pedido_kafka.exception.StatusPedidoInvalidoException;
import com.enzobf.cliente_pedido_kafka.exception.ValorInvalidoException;
import com.enzobf.cliente_pedido_kafka.kafka.producer.PedidoEventPublisher;
import com.enzobf.cliente_pedido_kafka.mapper.PedidoMapper;
import com.enzobf.cliente_pedido_kafka.repository.HistoricoPedidoRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;
import com.enzobf.cliente_pedido_kafka.strategy.StatusPedidoStrategy;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private HistoricoPedidoRepository historicoPedidoRepository;
    @Mock
    private ClienteService clienteService;
    @Mock
    private PedidoMapper pedidoMapper;
    @Mock
    private StatusPedidoStrategy statusPedidoStrategy;
    @Mock
    private PedidoEventPublisher pedidoEventPublisher;

    @InjectMocks
    private PedidoService pedidoService;

    private Cliente cliente;
    private PedidoRequest request;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(10L).nome("Ana").build();
        request = new PedidoRequest("Notebook", new BigDecimal("1000.00"), new BigDecimal("10"), 10L);
    }

    @Test
    void deveCadastrarPedidoComDescontoEEvento() {
        when(clienteService.buscarEntidade(10L)).thenReturn(cliente);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(5L);
            return pedido;
        });
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(
                new PedidoResponse(
                        5L,
                        "Notebook",
                        new BigDecimal("1000.00"),
                        new BigDecimal("10"),
                        new BigDecimal("900.00"),
                        LocalDateTime.now(),
                        StatusPedido.CRIADO,
                        10L,
                        "Ana"
                )
        );

        PedidoResponse response = pedidoService.cadastrar(request);

        assertThat(response.status()).isEqualTo(StatusPedido.CRIADO);
        assertThat(response.valorFinal()).isEqualByComparingTo("900.00");
        verify(historicoPedidoRepository).save(any());
        verify(pedidoEventPublisher).publicarPedidoCriado(any(PedidoCriadoEvent.class));
    }

    @Test
    void naoDeveCadastrarValorNegativo() {
        PedidoRequest invalido = new PedidoRequest("X", new BigDecimal("-1"), BigDecimal.ZERO, 10L);

        assertThatThrownBy(() -> pedidoService.cadastrar(invalido))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void deveAlterarStatusQuandoTransicaoForValida() {
        Pedido pedido = Pedido.builder()
                .id(5L)
                .status(StatusPedido.CRIADO)
                .cliente(cliente)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(statusPedidoStrategy.podeTransicionar(StatusPedido.CRIADO, StatusPedido.PROCESSANDO)).thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(
                new PedidoResponse(5L, "Notebook", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN,
                        LocalDateTime.now(), StatusPedido.PROCESSANDO, 10L, "Ana")
        );

        PedidoResponse response = pedidoService.alterarStatus(5L, StatusPedido.PROCESSANDO);

        assertThat(response.status()).isEqualTo(StatusPedido.PROCESSANDO);
        verify(pedidoEventPublisher).publicarStatusAlterado(any(PedidoStatusAlteradoEvent.class));
        verify(historicoPedidoRepository).save(any());
    }

    @Test
    void naoDeveAlterarStatusQuandoTransicaoForInvalida() {
        Pedido pedido = Pedido.builder()
                .id(5L)
                .status(StatusPedido.FINALIZADO)
                .cliente(cliente)
                .build();
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(statusPedidoStrategy.podeTransicionar(StatusPedido.FINALIZADO, StatusPedido.CANCELADO)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.alterarStatus(5L, StatusPedido.CANCELADO))
                .isInstanceOf(StatusPedidoInvalidoException.class);
    }
}
