package com.enzobf.cliente_pedido_kafka.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    @Test
    void deveListarClientesPaginado() {
        ClienteResponse cliente = new ClienteResponse(
                1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());

        given(clienteService.listar(eq(null), eq(null), any()))
                .willReturn(new PageImpl<>(List.of(cliente), PageRequest.of(0, 10), 1));

        assertThat(mockMvc.get().uri("/clientes"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].nome").isEqualTo("Enzo");
    }

    @Test
    void deveFiltrarClientesPorCpf() {
        ClienteResponse cliente = new ClienteResponse(
                1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());

        given(clienteService.listar(eq("12345678901"), eq(null), any()))
                .willReturn(new PageImpl<>(List.of(cliente)));

        assertThat(mockMvc.get().uri("/clientes?cpf=12345678901"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].cpf").isEqualTo("12345678901");
    }

    @Test
    void deveFiltrarClientesPorNome() {
        ClienteResponse cliente = new ClienteResponse(
                1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());

        given(clienteService.listar(eq(null), eq("Enzo"), any()))
                .willReturn(new PageImpl<>(List.of(cliente)));

        assertThat(mockMvc.get().uri("/clientes?nome=Enzo"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].nome").isEqualTo("Enzo");
    }

    @Test
    void deveAtualizarClienteRetornando200() {
        given(clienteService.atualizar(eq(1L), any())).willReturn(new ClienteResponse(
                1L, "Enzo Atualizado", "12345678901", "enzo@email.com", LocalDateTime.now()));

        assertThat(mockMvc.put().uri("/clientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Enzo Atualizado","cpf":"12345678901","email":"enzo@email.com"}
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.nome").isEqualTo("Enzo Atualizado");
    }

    @Test
    void deveRetornar404AoAtualizarClienteInexistente() {
        given(clienteService.atualizar(eq(99L), any()))
                .willThrow(new ClienteNaoEncontradoException(99L));

        assertThat(mockMvc.put().uri("/clientes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Enzo","cpf":"12345678901","email":"enzo@email.com"}
                        """))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveExcluirClienteRetornando204() {
        assertThat(mockMvc.delete().uri("/clientes/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void deveRetornar404AoExcluirClienteInexistente() {
        willThrow(new ClienteNaoEncontradoException(99L))
                .given(clienteService).excluir(99L);

        assertThat(mockMvc.delete().uri("/clientes/99"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
