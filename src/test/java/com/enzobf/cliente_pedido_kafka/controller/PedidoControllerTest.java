package com.enzobf.cliente_pedido_kafka.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
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
    void deveListarPedidosDoClientePaginado() {
        given(pedidoService.listar(eq(1L), eq(null), eq(null), any())).willReturn(new PageImpl<>(List.of(new PedidoResponse(
                10L, 1L, "Notebook", new BigDecimal("1000.00"),
                new BigDecimal("100.00"), new BigDecimal("900.00"), LocalDateTime.now()))));

        assertThat(mockMvc.get().uri("/pedidos?clienteId=1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].valorFinal").isEqualTo(900.00);
    }

    @Test
    void deveListarPedidosComFiltroDeValor() {
        given(pedidoService.listar(eq(null), eq(new BigDecimal("100")), eq(new BigDecimal("500")), any()))
                .willReturn(new PageImpl<>(List.of(new PedidoResponse(
                        11L, 2L, "Mouse", new BigDecimal("150.00"), null, null, LocalDateTime.now()))));

        assertThat(mockMvc.get().uri("/pedidos?valorMin=100&valorMax=500"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].id").isEqualTo(11);
    }

    @Test
    void deveAtualizarPedidoRetornando200() {
        given(pedidoService.atualizar(eq(10L), any())).willReturn(new PedidoResponse(
                10L, 1L, "Notebook Pro", new BigDecimal("1200.00"), null, null, LocalDateTime.now()));

        assertThat(mockMvc.put().uri("/pedidos/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":1,"descricao":"Notebook Pro","valor":1200.00}
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.descricao").isEqualTo("Notebook Pro");
    }

    @Test
    void deveRetornar404AoAtualizarPedidoInexistente() {
        given(pedidoService.atualizar(eq(99L), any()))
                .willThrow(new PedidoNaoEncontradoException(99L));

        assertThat(mockMvc.put().uri("/pedidos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"clienteId":1,"descricao":"Notebook","valor":1000.00}
                        """))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveExcluirPedidoRetornando204() {
        assertThat(mockMvc.delete().uri("/pedidos/10"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void deveRetornar404AoExcluirPedidoInexistente() {
        willThrow(new PedidoNaoEncontradoException(99L))
                .given(pedidoService).excluir(99L);

        assertThat(mockMvc.delete().uri("/pedidos/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
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
