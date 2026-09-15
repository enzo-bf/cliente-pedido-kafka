package com.enzobf.cliente_pedido_kafka.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculadoraDescontoTest {

    private final CalculadoraDesconto calculadoraDesconto = new CalculadoraDesconto();

    @ParameterizedTest
    @CsvSource({
            "100.00, 0.00",
            "499.99, 0.00",
            "500.00, 25.00",
            "999.99, 50.00",
            "1000.00, 100.00",
            "2500.55, 250.06"
    })
    void deveCalcularDescontoConformeFaixaDeValor(
            BigDecimal valor,
            BigDecimal descontoEsperado
    ) {
        assertThat(calculadoraDesconto.calcular(valor))
                .isEqualByComparingTo(descontoEsperado);
    }
}
