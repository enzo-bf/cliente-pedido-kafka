package com.enzobf.cliente_pedido_kafka.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regras de desconto aplicadas no processamento assíncrono do pedido:
 * valores a partir de 1000 recebem 10%, a partir de 500 recebem 5% e
 * os demais não recebem desconto.
 */
@Component
public class CalculadoraDesconto {

    private static final BigDecimal LIMITE_DESCONTO_MAIOR = new BigDecimal("1000.00");
    private static final BigDecimal LIMITE_DESCONTO_MENOR = new BigDecimal("500.00");
    private static final BigDecimal PERCENTUAL_MAIOR = new BigDecimal("0.10");
    private static final BigDecimal PERCENTUAL_MENOR = new BigDecimal("0.05");
    private static final int CASAS_DECIMAIS = 2;

    public BigDecimal calcular(BigDecimal valor) {
        BigDecimal percentual = percentualPara(valor);

        return valor
                .multiply(percentual)
                .setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }

    private BigDecimal percentualPara(BigDecimal valor) {
        if (valor.compareTo(LIMITE_DESCONTO_MAIOR) >= 0) {
            return PERCENTUAL_MAIOR;
        }

        if (valor.compareTo(LIMITE_DESCONTO_MENOR) >= 0) {
            return PERCENTUAL_MENOR;
        }

        return BigDecimal.ZERO;
    }
}
