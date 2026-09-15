package com.enzobf.cliente_pedido_kafka.specification;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.enzobf.cliente_pedido_kafka.entity.Cliente;

public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Cliente> comFiltros(
            String nome,
            String cpf,
            String email,
            LocalDate dataCadastroInicio,
            LocalDate dataCadastroFim
    ) {
        return Specification
                .where(porNome(nome))
                .and(porCpf(cpf))
                .and(porEmail(email))
                .and(porPeriodoCadastro(dataCadastroInicio, dataCadastroFim));
    }

    private static Specification<Cliente> porNome(String nome) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(nome)) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
        };
    }

    private static Specification<Cliente> porCpf(String cpf) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(cpf)) {
                return builder.conjunction();
            }
            return builder.like(root.get("cpf"), "%" + cpf + "%");
        };
    }

    private static Specification<Cliente> porEmail(String email) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(email)) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    private static Specification<Cliente> porPeriodoCadastro(LocalDate inicio, LocalDate fim) {
        return (root, query, builder) -> {
            if (inicio == null && fim == null) {
                return builder.conjunction();
            }
            if (inicio != null && fim != null) {
                return builder.between(
                        root.get("dataCadastro"),
                        inicio.atStartOfDay(),
                        fim.atTime(LocalTime.MAX)
                );
            }
            if (inicio != null) {
                return builder.greaterThanOrEqualTo(root.get("dataCadastro"), inicio.atStartOfDay());
            }
            return builder.lessThanOrEqualTo(root.get("dataCadastro"), fim.atTime(LocalTime.MAX));
        };
    }
}
