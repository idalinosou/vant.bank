package dev.vant.bank.painel.dto;

import dev.vant.bank.movimentacao.dto.MovimentacaoResponse;

import java.math.BigDecimal;
import java.util.List;

public record PainelResponse(
        long totalContas,
        long ativas,
        long bloqueadas,
        long encerradas,
        BigDecimal saldoTotal,
        BigDecimal volumeMes,
        long transferenciasMes,
        List<MovimentacaoResponse> ultimas) {
}
