package dev.vant.bank.painel;

import dev.vant.bank.BaseIntegracao;
import dev.vant.bank.conta.Conta;
import dev.vant.bank.conta.StatusConta;
import dev.vant.bank.conta.TipoConta;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PainelIntegrationTest extends BaseIntegracao {

    @Test
    void resumeContasESaldoSemEncerradas() throws Exception {
        conta("1001-1", "Ana", TipoConta.CORRENTE, "100.00", "0.00", StatusConta.ATIVA);
        conta("1002-2", "Bruno", TipoConta.POUPANCA, "50.00", "0.00", StatusConta.ATIVA);
        conta("1003-3", "Carla", TipoConta.CORRENTE, "25.00", "0.00", StatusConta.BLOQUEADA);
        conta("1004-4", "Diego", TipoConta.POUPANCA, "999.00", "0.00", StatusConta.ENCERRADA);

        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalContas").value(4))
                .andExpect(jsonPath("$.ativas").value(2))
                .andExpect(jsonPath("$.bloqueadas").value(1))
                .andExpect(jsonPath("$.encerradas").value(1));

        BigDecimal saldo = contas.somarSaldos();
        assertEquals(0, new BigDecimal("175.00").compareTo(saldo));
    }

    @Test
    void somaVolumeEDeTransferenciasApenasDoMesAtual() throws Exception {
        Conta conta = conta("2001-1");
        YearMonth atual = YearMonth.now();
        YearMonth anterior = atual.minusMonths(1);

        movimentacao(conta, TipoMovimentacao.DEPOSITO, "100.00", atual, Math.min(2, atual.lengthOfMonth()));
        movimentacao(conta, TipoMovimentacao.SAQUE, "40.00", atual, Math.min(3, atual.lengthOfMonth()));
        movimentacao(conta, TipoMovimentacao.TRANSFERENCIA_ENVIADA, "30.00", atual, Math.min(4, atual.lengthOfMonth()));
        movimentacao(conta, TipoMovimentacao.DEPOSITO, "1000.00", anterior, 10);

        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeMes").value(170.00))
                .andExpect(jsonPath("$.transferenciasMes").value(1));
    }

    @Test
    void listaAsSeisUltimasMovimentacoes() throws Exception {
        Conta conta = conta("3001-1");
        YearMonth atual = YearMonth.now();
        for (int dia = 1; dia <= 7; dia++) {
            movimentacao(conta, TipoMovimentacao.DEPOSITO, "10.00", atual, dia);
        }

        mvc.perform(get("/api/painel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimas.length()").value(6))
                .andExpect(jsonPath("$.ultimas[0].contaNumero").value("3001-1"));
    }
}
