package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.ClientePossuiPedidosException;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.exception.EmailJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.mapper.ClienteMapper;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest request;

    @BeforeEach
    void setUp() {
        request = new ClienteRequest("Ana Silva", "12345678901", "ana@email.com");
    }

    @Test
    void deveCadastrarCliente() {
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });
        when(clienteMapper.toResponse(any(Cliente.class))).thenReturn(
                new ClienteResponse(1L, "Ana Silva", "12345678901", "ana@email.com", LocalDateTime.now())
        );

        ClienteResponse response = clienteService.cadastrar(request);

        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@email.com");
    }

    @Test
    void naoDeveCadastrarCpfDuplicado() {
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(CpfJaCadastradoException.class);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        when(clienteRepository.existsByCpf("12345678901")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void deveLancarErroQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }

    @Test
    void naoDeveExcluirClienteComPedidos() {
        Cliente cliente = Cliente.builder().id(1L).nome("Ana").build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidoRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.excluir(1L))
                .isInstanceOf(ClientePossuiPedidosException.class);
        verify(clienteRepository, never()).delete(cliente);
    }
}
