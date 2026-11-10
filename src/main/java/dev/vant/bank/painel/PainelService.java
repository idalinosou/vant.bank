package dev.vant.bank.painel;

import dev.vant.bank.conta.ContaRepository;
import dev.vant.bank.conta.StatusConta;
import dev.vant.bank.movimentacao.MovimentacaoRepository;
import dev.vant.bank.movimentacao.MovimentacaoService;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import dev.vant.bank.painel.dto.PainelResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
public class PainelService {

    private final ContaRepository contas;
    private final MovimentacaoRepository movimentacoes;
    private final MovimentacaoService servicoMovimentacoes;

    public PainelService(ContaRepository contas, MovimentacaoRepository movimentacoes,
                         MovimentacaoService servicoMovimentacoes) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
        this.servicoMovimentacoes = servicoMovimentacoes;
    }

    @Transactional(readOnly = true)
    public PainelResponse resumo() {
        ZoneId fuso = ZoneId.systemDefault();
        YearMonth mes = YearMonth.now(fuso);
        Instant inicio = mes.atDay(1).atStartOfDay(fuso).toInstant();
        Instant fim = mes.plusMonths(1).atDay(1).atStartOfDay(fuso).toInstant();

        return new PainelResponse(
                contas.count(),
                contas.countByStatus(StatusConta.ATIVA),
                contas.countByStatus(StatusConta.BLOQUEADA),
                contas.countByStatus(StatusConta.ENCERRADA),
                contas.somarSaldos(),
                movimentacoes.somarVolume(inicio, fim),
                movimentacoes.countByTipoAndCriadaEmGreaterThanEqualAndCriadaEmLessThan(
                        TipoMovimentacao.TRANSFERENCIA_ENVIADA, inicio, fim),
                servicoMovimentacoes.ultimas());
    }
}
