package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

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
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCadastrarClienteComDataDeCadastro() {
        ClienteRequest request = new ClienteRequest("Enzo", "12345678901", "enzo@email.com");

        when(clienteRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setId(1L);
                    return cliente;
                });

        ClienteResponse response = clienteService.cadastrar(request);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());

        assertThat(captor.getValue().getDataCadastro()).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Enzo");
        assertThat(response.cpf()).isEqualTo("12345678901");
        assertThat(response.email()).isEqualTo("enzo@email.com");
    }

    @Test
    void naoDeveCadastrarClienteComCpfDuplicado() {
        ClienteRequest request = new ClienteRequest("Enzo", "12345678901", "enzo@email.com");

        when(clienteRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.cadastrar(request))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorId() {
        Cliente cliente = new Cliente(1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThat(clienteService.buscarPorId(1L).nome()).isEqualTo("Enzo");
    }

    @Test
    void deveFalharAoBuscarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }
}
