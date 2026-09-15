package com.enzobf.cliente_pedido_kafka.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.dto.response.PedidoResponse;
import com.enzobf.cliente_pedido_kafka.exception.PedidoNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void deveCadastrarPedidoRetornando201() {
        given(pedidoService.cadastrar(any())).willReturn(new PedidoResponse(
                10L, 1L, "Notebook", new BigDecimal("1000.00"), null, null, LocalDateTime.now()));

        assertThat(mockMvc.post().uri("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":1,"descricao":"Notebook","valor":1000.00}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.id").isEqualTo(10);
    }

    @Test
    void deveRetornar400QuandoValorInvalido() {
        assertThat(mockMvc.post().uri("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":1,"descricao":"Notebook","valor":0}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deveRetornar404QuandoPedidoNaoExiste() {
        given(pedidoService.buscarPorId(99L))
                .willThrow(new PedidoNaoEncontradoException(99L));

        assertThat(mockMvc.get().uri("/pedidos/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveListarPedidosDoCliente() {
        given(pedidoService.listarPorCliente(1L)).willReturn(List.of(new PedidoResponse(
                10L, 1L, "Notebook", new BigDecimal("1000.00"),
                new BigDecimal("100.00"), new BigDecimal("900.00"), LocalDateTime.now())));

        assertThat(mockMvc.get().uri("/pedidos?clienteId=1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].valorFinal").isEqualTo(900.00);
    }

    @Test
    void deveListarHistoricoDoPedido() {
        given(pedidoService.listarHistorico(10L)).willReturn(List.of(new HistoricoPedidoResponse(
                1L, 10L, "PEDIDO_PROCESSADO", LocalDateTime.now(), "Desconto aplicado")));

        assertThat(mockMvc.get().uri("/pedidos/10/historico"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].tipoEvento").isEqualTo("PEDIDO_PROCESSADO");
    }
}
