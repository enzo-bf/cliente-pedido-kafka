package com.enzobf.cliente_pedido_kafka.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;

/**
 * Testa os filtros combináveis (CPF, nome) e a paginação/ordenação da
 * listagem de clientes contra um banco H2 real, exercitando a
 * {@link ClienteSpecification} de ponta a ponta via {@link ClienteRepository}.
 *
 * <p>Os testes filtram/identificam os clientes pelos próprios CPFs ou por um
 * marcador único no nome, em vez de assumir uma tabela vazia. O H2 de teste
 * ({@code jdbc:h2:mem:testdb}) é compartilhado por toda a suíte, e o
 * {@link com.enzobf.cliente_pedido_kafka.messaging.PedidoEventIntegrationTest}
 * grava clientes de forma assíncrona (consumer Kafka em outra thread), fora
 * de qualquer transação — logo não é revertido pelo {@code @Transactional}
 * desta classe e não pode ser removido aqui via {@code deleteAll()} sem
 * violar a FK de {@code Pedido -> Cliente} criada por aquele teste.</p>
 */
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"
})
@Transactional
class ClienteRepositorySpecificationTest {

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente salvar(String nome, String cpf) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setEmail(cpf + "@email.com");
        cliente.setDataCadastro(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    @Test
    void deveFiltrarPorCpfExato() {
        salvar("Enzo", "11111111111");
        salvar("Bruno", "22222222222");

        Page<Cliente> pagina = clienteRepository.findAll(
                ClienteSpecification.comFiltros("22222222222", null),
                PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().get(0).getNome()).isEqualTo("Bruno");
    }

    @Test
    void deveFiltrarPorNomeContendoIgnorandoCaixa() {
        salvar("Enzo Ferreira", "11111111111");
        salvar("Bruno Enzoni", "22222222222");
        salvar("Carla Silva", "33333333333");

        Page<Cliente> pagina = clienteRepository.findAll(
                ClienteSpecification.comFiltros(null, "enzo"),
                PageRequest.of(0, 10));

        assertThat(pagina.getContent())
                .extracting(Cliente::getCpf)
                .contains("11111111111", "22222222222");
    }

    @Test
    void devePaginarEOrdenarResultados() {
        salvar("PagTeste Carlos", "10000000001");
        salvar("PagTeste Ana", "10000000002");
        salvar("PagTeste Bruno", "10000000003");

        Page<Cliente> primeiraPagina = clienteRepository.findAll(
                ClienteSpecification.comFiltros(null, "PagTeste"),
                PageRequest.of(0, 2, Sort.by("nome").ascending()));

        assertThat(primeiraPagina.getTotalElements()).isEqualTo(3);
        assertThat(primeiraPagina.getContent()).hasSize(2);
        assertThat(primeiraPagina.getContent().get(0).getNome()).isEqualTo("PagTeste Ana");
        assertThat(primeiraPagina.getContent().get(1).getNome()).isEqualTo("PagTeste Bruno");
        assertThat(primeiraPagina.getTotalPages()).isEqualTo(2);
    }
}
