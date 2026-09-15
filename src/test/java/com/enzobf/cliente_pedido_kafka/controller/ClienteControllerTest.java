package com.enzobf.cliente_pedido_kafka.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void deveCadastrarClienteRetornando201() {
        given(clienteService.cadastrar(any())).willReturn(new ClienteResponse(
                1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now()));

        assertThat(mockMvc.post().uri("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Enzo","cpf":"12345678901","email":"enzo@email.com"}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.id").isEqualTo(1);
    }

    @Test
    void deveRetornar400QuandoPayloadInvalido() {
        assertThat(mockMvc.post().uri("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"","cpf":"12345678901","email":"email-invalido"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deveRetornar409QuandoCpfDuplicado() {
        willThrow(new CpfJaCadastradoException("12345678901"))
                .given(clienteService).cadastrar(any());

        assertThat(mockMvc.post().uri("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Enzo","cpf":"12345678901","email":"enzo@email.com"}
                        """))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() {
        given(clienteService.buscarPorId(99L))
                .willThrow(new ClienteNaoEncontradoException(99L));

        assertThat(mockMvc.get().uri("/clientes/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
