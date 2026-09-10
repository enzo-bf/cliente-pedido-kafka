package com.enzobf.cliente_pedido_kafka.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse cadastrar(ClienteRequest request) {
        validarCpfDuplicado(request.cpf());

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email());
        cliente.setDataCadastro(LocalDateTime.now());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);
    }
    public ClienteResponse buscarPorId(Long id) {
    Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ClienteNaoEncontradoException(id));

    return converterParaResponse(cliente);}

    private void validarCpfDuplicado(String cpf) {
        if (clienteRepository.existsByCpf(cpf)) {
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