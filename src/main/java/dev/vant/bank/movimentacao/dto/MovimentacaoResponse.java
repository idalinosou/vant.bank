package dev.vant.bank.movimentacao.dto;

import dev.vant.bank.movimentacao.Movimentacao;
import dev.vant.bank.movimentacao.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoResponse(
        Long id,
        Long contaId,
        String contaNumero,
        String contaTitular,
        TipoMovimentacao tipo,
        String tipoRotulo,
        boolean entrada,
        BigDecimal valor,
        BigDecimal saldoPosterior,
        String descricao,
        String contraparte,
        Instant criadaEm) {

    public static MovimentacaoResponse de(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getConta().getId(),
                movimentacao.getConta().getNumero(),
                movimentacao.getConta().getTitular(),
                movimentacao.getTipo(),
                movimentacao.getTipo().getRotulo(),
                movimentacao.getTipo().isEntrada(),
                movimentacao.getValor(),
                movimentacao.getSaldoPosterior(),
                movimentacao.getDescricao(),
                movimentacao.getContraparte(),
                movimentacao.getCriadaEm());
    }
}
