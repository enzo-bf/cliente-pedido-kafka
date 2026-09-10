package com.enzobf.cliente_pedido_kafka.service;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteResponse cadastrar(ClienteRequest request) {
        verificarCpfDuplicado(request.cpf());

        Cliente cliente = Cliente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .email(request.email())
                .dataCadastro(LocalDateTime.now())
                .build();

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);
    }

    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));

        return converterParaResponse(cliente);
    }

    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository
                .findAll(pageable)
                .map(this::converterParaResponse);
    }

    private void verificarCpfDuplicado(String cpf) {
        if (clienteRepository.findByCpf(cpf).isPresent()) {
            throw new CpfJaCadastradoException(cpf);
        }
    }

    private ClienteResponse converterParaResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getDataCadastro()
        );
    }
}