package com.enzobf.cliente_pedido_kafka.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.dto.request.ClienteRequest;
import com.enzobf.cliente_pedido_kafka.dto.response.ClienteResponse;
import com.enzobf.cliente_pedido_kafka.entity.Cliente;
import com.enzobf.cliente_pedido_kafka.exception.ClienteNaoEncontradoException;
import com.enzobf.cliente_pedido_kafka.exception.CpfJaCadastradoException;
import com.enzobf.cliente_pedido_kafka.repository.ClienteRepository;
import com.enzobf.cliente_pedido_kafka.repository.ClienteSpecification;

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

        return converterParaResponse(cliente);
    }

    /**
     * Lista clientes de forma paginada, com filtros combináveis e
     * opcionais por CPF (igualdade) e nome (contém, sem distinção de
     * caixa). A ordenação e a paginação vêm do {@link Pageable} recebido
     * do controller (parâmetros {@code page}, {@code size} e {@code sort}).
     */
    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(String cpf, String nome, Pageable pageable) {
        Specification<Cliente> filtros = ClienteSpecification.comFiltros(cpf, nome);

        return clienteRepository.findAll(filtros, pageable)
                .map(this::converterParaResponse);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));

        if (!cliente.getCpf().equals(request.cpf())) {
            validarCpfDuplicado(request.cpf());
        }

        cliente.setNome(request.nome());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email());

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return converterParaResponse(clienteAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNaoEncontradoException(id);
        }

        clienteRepository.deleteById(id);
    }

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