package dev.vant.bank.conta.dto;

import dev.vant.bank.conta.Conta;
import dev.vant.bank.conta.StatusConta;
import dev.vant.bank.conta.TipoConta;

import java.math.BigDecimal;
import java.time.Instant;

public record ContaResponse(
        Long id,
        String numero,
        String titular,
        String documento,
        TipoConta tipo,
        String tipoRotulo,
        StatusConta status,
        String statusRotulo,
        BigDecimal saldo,
        BigDecimal limite,
        Instant criadaEm) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getTitular(),
                conta.getDocumento(),
                conta.getTipo(),
                conta.getTipo().getRotulo(),
                conta.getStatus(),
                conta.getStatus().getRotulo(),
                conta.getSaldo(),
                conta.getLimite(),
                conta.getCriadaEm());
    }
}
