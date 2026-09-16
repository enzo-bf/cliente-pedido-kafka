package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    @Test
    void deveListarClientesPaginado() {
        Cliente cliente = new Cliente(1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);

        when(clienteRepository.findAll(ArgumentMatchers.<Specification<Cliente>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(cliente), pageable, 1));

        var pagina = clienteService.listar(null, null, pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().get(0).nome()).isEqualTo("Enzo");
    }

    @Test
    void deveAtualizarCliente() {
        Cliente cliente = new Cliente(1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());
        ClienteRequest request = new ClienteRequest("Enzo Atualizado", "12345678901", "novo@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Enzo Atualizado");
        assertThat(response.email()).isEqualTo("novo@email.com");
        verify(clienteRepository, never()).existsByCpf(any());
    }

    @Test
    void deveValidarCpfDuplicadoAoAtualizarComCpfDiferente() {
        Cliente cliente = new Cliente(1L, "Enzo", "12345678901", "enzo@email.com", LocalDateTime.now());
        ClienteRequest request = new ClienteRequest("Enzo", "99999999999", "enzo@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCpf("99999999999")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.atualizar(1L, request))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveFalharAoAtualizarClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        ClienteRequest request = new ClienteRequest("Enzo", "12345678901", "enzo@email.com");

        assertThatThrownBy(() -> clienteService.atualizar(99L, request))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }

    @Test
    void deveExcluirCliente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        clienteService.excluir(1L);

        verify(clienteRepository).deleteById(1L);
    }

    @Test
    void deveFalharAoExcluirClienteInexistente() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.excluir(99L))
                .isInstanceOf(ClienteNaoEncontradoException.class);

        verify(clienteRepository, never()).deleteById(any());
    }
}
