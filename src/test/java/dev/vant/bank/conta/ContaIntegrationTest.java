package dev.vant.bank.conta;

import dev.vant.bank.BaseIntegracao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContaIntegrationTest extends BaseIntegracao {

    @Test
    void criaContaComSaldoZerado() throws Exception {
        mvc.perform(post("/api/contas")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "numero", "2001-1",
                                "titular", "Marina Alves",
                                "documento", "111.222.333-44",
                                "tipo", "CORRENTE",
                                "limite", 1500.00))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/contas/")))
                .andExpect(jsonPath("$.numero").value("2001-1"))
                .andExpect(jsonPath("$.titular").value("Marina Alves"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.saldo").value(0));

        Conta conta = contas.findAll().getFirst();
        assertEquals(0, new BigDecimal("0.00").compareTo(conta.getSaldo()));
    }

    @Test
    void poupancaNaoAceitaLimite() throws Exception {
        mvc.perform(post("/api/contas")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "numero", "2002-2",
                                "titular", "Poupança Teste",
                                "tipo", "POUPANCA",
                                "limite", 900.00))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.limite").value(0));
    }

    @Test
    void naoCriaContaComNumeroDuplicado() throws Exception {
        conta("2003-3");

        mvc.perform(post("/api/contas")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "numero", "2003-3",
                                "titular", "Outra Pessoa",
                                "tipo", "CORRENTE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Já existe uma conta")));
    }

    @Test
    void naoCriaContaSemTitular() throws Exception {
        mvc.perform(post("/api/contas")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "numero", "2004-4",
                                "titular", "",
                                "tipo", "CORRENTE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titular").exists());
    }

    @Test
    void listaComBuscaStatusEPaginacao() throws Exception {
        conta("3001-1", "Ana Souza", TipoConta.CORRENTE, "100.00", "0.00", StatusConta.ATIVA);
        conta("3002-2", "Bruno Lima", TipoConta.CORRENTE, "200.00", "0.00", StatusConta.ATIVA);
        conta("3003-3", "Carla Dias", TipoConta.CORRENTE, "300.00", "0.00", StatusConta.BLOQUEADA);

        mvc.perform(get("/api/contas").param("busca", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].numero").value("3001-1"));

        mvc.perform(get("/api/contas").param("status", "BLOQUEADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].numero").value("3003-3"));

        mvc.perform(get("/api/contas").param("pagina", "1").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void atualizaTitularELimite() throws Exception {
        Conta conta = conta("4001-1", "Nome Antigo", TipoConta.CORRENTE, "50.00", "0.00", StatusConta.ATIVA);

        mvc.perform(put("/api/contas/" + conta.getId())
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of(
                                "titular", "Nome Novo",
                                "documento", "999.888.777-66",
                                "tipo", "CORRENTE",
                                "limite", 2500.00))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titular").value("Nome Novo"))
                .andExpect(jsonPath("$.documento").value("999.888.777-66"));

        Conta atualizada = contas.findById(conta.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("2500.00").compareTo(atualizada.getLimite()));
    }

    @Test
    void bloqueiaEDesbloqueiaConta() throws Exception {
        Conta conta = conta("5001-1");

        mvc.perform(patch("/api/contas/" + conta.getId() + "/status")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("status", "BLOQUEADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADA"))
                .andExpect(jsonPath("$.statusRotulo").value("Bloqueada"));

        mvc.perform(patch("/api/contas/" + conta.getId() + "/status")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("status", "ATIVA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void naoEncerraContaComSaldo() throws Exception {
        Conta conta = conta("6001-1", "Com Saldo", TipoConta.CORRENTE, "10.00", "0.00", StatusConta.ATIVA);

        mvc.perform(patch("/api/contas/" + conta.getId() + "/status")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("status", "ENCERRADA"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("saldo zerado")));
    }

    @Test
    void encerraContaComSaldoZerado() throws Exception {
        Conta conta = conta("6002-2");

        mvc.perform(patch("/api/contas/" + conta.getId() + "/status")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("status", "ENCERRADA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
    }

    @Test
    void naoAlteraContaEncerrada() throws Exception {
        Conta conta = conta("6003-3", "Encerrada", TipoConta.POUPANCA, "0.00", "0.00", StatusConta.ENCERRADA);

        mvc.perform(put("/api/contas/" + conta.getId())
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("titular", "Tentativa", "tipo", "POUPANCA"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("encerrada")));
    }

    @Test
    void naoReabreContaEncerrada() throws Exception {
        Conta conta = conta("6004-4", "Encerrada", TipoConta.POUPANCA, "0.00", "0.00", StatusConta.ENCERRADA);

        mvc.perform(patch("/api/contas/" + conta.getId() + "/status")
                        .contentType(APPLICATION_JSON)
                        .content(json(Map.of("status", "ATIVA"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Não é possível alterar o status")));
    }
}
