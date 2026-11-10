package dev.vant.bank.transferencia;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferenciaRequest(
        @NotNull Long origemId,
        @NotNull Long destinoId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal valor,
        @Size(max = 160) String descricao) {
}
