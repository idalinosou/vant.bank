package dev.vant.bank.conta.dto;

import dev.vant.bank.conta.StatusConta;
import jakarta.validation.constraints.NotNull;

public record StatusContaRequest(@NotNull StatusConta status) {
}
