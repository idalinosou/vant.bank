package dev.vant.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vant.bank.conta.Conta;
import dev.vant.bank.conta.ContaRepository;
import dev.vant.bank.conta.StatusConta;
import dev.vant.bank.conta.TipoConta;
import dev.vant.bank.movimentacao.Movimentacao;
import dev.vant.bank.movimentacao.MovimentacaoRepository;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegracao {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected ContaRepository contas;

    @Autowired
    protected MovimentacaoRepository movimentacoes;

    protected Conta conta(String numero) {
        return conta(numero, "Titular " + numero, TipoConta.CORRENTE, "0.00", "0.00", StatusConta.ATIVA);
    }

    protected Conta conta(String numero, String titular, TipoConta tipo,
                          String saldo, String limite, StatusConta status) {
        Conta conta = new Conta(numero, titular, "000.000.000-00", tipo, new BigDecimal(limite));
        conta.setSaldo(new BigDecimal(saldo));
        conta.setStatus(status);
        return contas.save(conta);
    }

    protected Movimentacao movimentacao(Conta conta, TipoMovimentacao tipo, String valor,
                                        YearMonth mes, int dia) {
        Movimentacao movimentacao = new Movimentacao(
                conta, tipo, new BigDecimal(valor), conta.getSaldo(), "Movimentação de teste", null);
        movimentacao.setCriadaEm(instante(mes, dia));
        return movimentacoes.save(movimentacao);
    }

    protected Instant instante(YearMonth mes, int dia) {
        return mes.atDay(dia).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant();
    }

    protected String json(Object valor) throws Exception {
        return json.writeValueAsString(valor);
    }
}
