package com.enzobf.cliente_pedido_kafka.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.enzobf.cliente_pedido_kafka.specification.ClienteSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        verificarCpfDuplicado(request.cpf());
        verificarEmailDuplicado(request.email());

        Cliente cliente = Cliente.builder()
                .nome(request.nome().trim())
                .cpf(request.cpf())
                .email(request.email().trim().toLowerCase())
                .dataCadastro(LocalDateTime.now())
                .build();

        Cliente clienteSalvo = clienteRepository.save(cliente);
        log.info("Cliente cadastrado id={} cpf={}", clienteSalvo.getId(), clienteSalvo.getCpf());
        return clienteMapper.toResponse(clienteSalvo);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(
            String nome,
            String cpf,
            String email,
            LocalDate dataCadastroInicio,
            LocalDate dataCadastroFim,
            Pageable pageable
    ) {
        return clienteRepository
                .findAll(
                        ClienteSpecification.comFiltros(nome, cpf, email, dataCadastroInicio, dataCadastroFim),
                        pageable
                )
                .map(clienteMapper::toResponse);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);

        if (clienteRepository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new CpfJaCadastradoException(request.cpf());
        }
        if (clienteRepository.existsByEmailAndIdNot(request.email().trim().toLowerCase(), id)) {
            throw new EmailJaCadastradoException(request.email());
        }

        cliente.setNome(request.nome().trim());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email().trim().toLowerCase());

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        log.info("Cliente atualizado id={}", clienteAtualizado.getId());
        return clienteMapper.toResponse(clienteAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new ClientePossuiPedidosException(id);
        }
        clienteRepository.delete(cliente);
        log.info("Cliente excluído id={}", id);
    }

    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    private void verificarCpfDuplicado(String cpf) {
        if (clienteRepository.existsByCpf(cpf)) {
            throw new CpfJaCadastradoException(cpf);
        }
    }

    private void verificarEmailDuplicado(String email) {
        if (clienteRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new EmailJaCadastradoException(email);
        }
    }
}
