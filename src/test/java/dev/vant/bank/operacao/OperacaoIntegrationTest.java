package dev.vant.bank.operacao;

import dev.vant.bank.BaseIntegracao;
import dev.vant.bank.conta.Conta;
import dev.vant.bank.conta.StatusConta;
import dev.vant.bank.conta.TipoConta;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OperacaoIntegrationTest extends BaseIntegracao {

    @Test
    void depositaESomaSaldo() throws Exception {
        Conta conta = conta("7001-1", "Ana", TipoConta.CORRENTE, "100.00", "0.00", StatusConta.ATIVA);

        mvc.perform(post("/api/contas/" + conta.getId() + "/depositos")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("valor", 250.50, "descricao", "Depósito de teste"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(350.50));

        Conta atualizada = contas.findById(conta.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("350.50").compareTo(atualizada.getSaldo()));
        assertEquals(1, movimentacoes.count());
        assertEquals(TipoMovimentacao.DEPOSITO, movimentacoes.findAll().getFirst().getTipo());
    }

    @Test
    void sacaSomaSaldoNegativoQuandoUsaLimite() throws Exception {
        Conta conta = conta("7002-2", "Bruno", TipoConta.CORRENTE, "100.00", "500.00", StatusConta.ATIVA);

        mvc.perform(post("/api/contas/" + conta.getId() + "/saques")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("valor", 400.00))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(-300.00));

        Conta atualizada = contas.findById(conta.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("-300.00").compareTo(atualizada.getSaldo()));
    }

    @Test
    void bloqueiaSaqueAcimaDoSaldoELimite() throws Exception {
        Conta conta = conta("7003-3", "Carla", TipoConta.CORRENTE, "100.00", "500.00", StatusConta.ATIVA);

        mvc.perform(post("/api/contas/" + conta.getId() + "/saques")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("valor", 601.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("insuficientes")));
    }

    @Test
    void naoDepositaEmContaBloqueada() throws Exception {
        Conta conta = conta("7004-4", "Diego", TipoConta.CORRENTE, "50.00", "0.00", StatusConta.BLOQUEADA);

        mvc.perform(post("/api/contas/" + conta.getId() + "/depositos")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("valor", 10.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("não está ativa")));
    }

    @Test
    void rejeitaDepositoComValorInvalido() throws Exception {
        Conta conta = conta("7005-5");

        mvc.perform(post("/api/contas/" + conta.getId() + "/depositos")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("valor", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    void transfereEntreContasEGeraDuasMovimentacoes() throws Exception {
        Conta origem = conta("8001-1", "Origem", TipoConta.CORRENTE, "1000.00", "0.00", StatusConta.ATIVA);
        Conta destino = conta("8002-2", "Destino", TipoConta.POUPANCA, "100.00", "0.00", StatusConta.ATIVA);

        mvc.perform(post("/api/transferencias")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "origemId", origem.getId(),
                                "destinoId", destino.getId(),
                                "valor", 250.00,
                                "descricao", "Aluguel"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origemNumero").value("8001-1"))
                .andExpect(jsonPath("$.destinoNumero").value("8002-2"))
                .andExpect(jsonPath("$.saldoOrigem").value(750.00))
                .andExpect(jsonPath("$.saldoDestino").value(350.00));

        assertEquals(0, new BigDecimal("750.00").compareTo(contas.findById(origem.getId()).orElseThrow().getSaldo()));
        assertEquals(0, new BigDecimal("350.00").compareTo(contas.findById(destino.getId()).orElseThrow().getSaldo()));
        assertEquals(2, movimentacoes.count());
    }

    @Test
    void naoTransfereParaMesmaConta() throws Exception {
        Conta conta = conta("8003-3", "Mesma", TipoConta.CORRENTE, "1000.00", "0.00", StatusConta.ATIVA);

        mvc.perform(post("/api/transferencias")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "origemId", conta.getId(),
                                "destinoId", conta.getId(),
                                "valor", 10.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("diferente")));
    }

    @Test
    void naoTransfereParaContaBloqueada() throws Exception {
        Conta origem = conta("8004-4", "Origem", TipoConta.CORRENTE, "1000.00", "0.00", StatusConta.ATIVA);
        Conta destino = conta("8005-5", "Bloqueada", TipoConta.CORRENTE, "0.00", "0.00", StatusConta.BLOQUEADA);

        mvc.perform(post("/api/transferencias")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "origemId", origem.getId(),
                                "destinoId", destino.getId(),
                                "valor", 10.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("destino")));
    }

    @Test
    void naoTransfereSemSaldoELimite() throws Exception {
        Conta origem = conta("8006-6", "Sem Saldo", TipoConta.CORRENTE, "50.00", "100.00", StatusConta.ATIVA);
        Conta destino = conta("8007-7", "Destino", TipoConta.CORRENTE, "0.00", "0.00", StatusConta.ATIVA);

        mvc.perform(post("/api/transferencias")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "origemId", origem.getId(),
                                "destinoId", destino.getId(),
                                "valor", 200.00))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("insuficientes")));
    }

    @Test
    void extratoPaginaEFiltraPorTipo() throws Exception {
        Conta conta = conta("9001-1");
        YearMonth mes = YearMonth.now();
        movimentacao(conta, TipoMovimentacao.DEPOSITO, "100.00", mes, Math.min(2, mes.lengthOfMonth()));
        movimentacao(conta, TipoMovimentacao.SAQUE, "20.00", mes, Math.min(3, mes.lengthOfMonth()));
        movimentacao(conta, TipoMovimentacao.DEPOSITO, "50.00", mes, Math.min(4, mes.lengthOfMonth()));

        mvc.perform(get("/api/contas/" + conta.getId() + "/extrato")
                        .param("pagina", "1")
                        .param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));

        mvc.perform(get("/api/contas/" + conta.getId() + "/extrato")
                        .param("tipo", "DEPOSITO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("DEPOSITO"));
    }

    @Test
    void extratoDeContaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/contas/999999/extrato"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", containsString("Conta não encontrada")));
    }
}
