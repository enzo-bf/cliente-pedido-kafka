package com.enzobf.cliente_pedido_kafka.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.enzobf.cliente_pedido_kafka.dto.response.HistoricoPedidoResponse;
import com.enzobf.cliente_pedido_kafka.service.PedidoService;

@WebMvcTest(HistoricoPedidoController.class)
class HistoricoPedidoControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void deveListarHistoricoFiltradoPorTipoEvento() {
        given(pedidoService.listarHistoricoPorTipoEvento(eq("PEDIDO_PROCESSADO"), any()))
                .willReturn(new PageImpl<>(List.of(new HistoricoPedidoResponse(
                        1L, 10L, "PEDIDO_PROCESSADO", LocalDateTime.now(), "Desconto aplicado"))));

        assertThat(mockMvc.get().uri("/historico-pedidos?tipoEvento=PEDIDO_PROCESSADO"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].tipoEvento").isEqualTo("PEDIDO_PROCESSADO");
    }

    @Test
    void deveListarTodoHistoricoQuandoTipoEventoNaoInformado() {
        given(pedidoService.listarHistoricoPorTipoEvento(eq(null), any()))
                .willReturn(new PageImpl<>(List.of(
                        new HistoricoPedidoResponse(1L, 10L, "PEDIDO_PROCESSADO", LocalDateTime.now(), "Desconto aplicado"),
                        new HistoricoPedidoResponse(2L, 11L, "PEDIDO_ATUALIZADO", LocalDateTime.now(), "Desconto recalculado")
                )));

        assertThat(mockMvc.get().uri("/historico-pedidos"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[1].tipoEvento").isEqualTo("PEDIDO_ATUALIZADO");
    }
}
