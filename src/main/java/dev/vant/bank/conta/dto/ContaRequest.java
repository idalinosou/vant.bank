package dev.vant.bank.conta.dto;

import dev.vant.bank.conta.TipoConta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ContaRequest(
        @NotBlank @Size(max = 20) String numero,
        @NotBlank @Size(max = 140) String titular,
        @Size(max = 30) String documento,
        @NotNull TipoConta tipo,
        @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal limite) {
}
