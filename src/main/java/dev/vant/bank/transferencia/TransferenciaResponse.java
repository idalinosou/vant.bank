package dev.vant.bank.transferencia;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferenciaResponse(
        String origemNumero,
        String origemTitular,
        String destinoNumero,
        String destinoTitular,
        BigDecimal valor,
        BigDecimal saldoOrigem,
        BigDecimal saldoDestino,
        String descricao,
        Instant criadaEm) {
}
