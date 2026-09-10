package com.enzobf.cliente_pedido_kafka.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

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