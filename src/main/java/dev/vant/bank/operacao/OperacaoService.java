package dev.vant.bank.operacao;

import dev.vant.bank.conta.Conta;
import dev.vant.bank.conta.ContaRepository;
import dev.vant.bank.conta.dto.ContaResponse;
import dev.vant.bank.erro.RecursoNaoEncontradoException;
import dev.vant.bank.erro.RegraDeNegocioException;
import dev.vant.bank.movimentacao.Movimentacao;
import dev.vant.bank.movimentacao.MovimentacaoRepository;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import dev.vant.bank.transferencia.TransferenciaRequest;
import dev.vant.bank.transferencia.TransferenciaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class OperacaoService {

    private final ContaRepository contas;
    private final MovimentacaoRepository movimentacoes;

    public OperacaoService(ContaRepository contas, MovimentacaoRepository movimentacoes) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
    }

    @Transactional
    public ContaResponse depositar(Long contaId, OperacaoRequest pedido) {
        Conta conta = bloqueada(contaId);
        exigirAtiva(conta);
        BigDecimal valor = normalizar(pedido.valor());
        conta.depositar(valor);
        contas.save(conta);
        registrar(conta, TipoMovimentacao.DEPOSITO, valor, descricaoOu(pedido.descricao(), "Depósito em dinheiro"), null);
        return ContaResponse.de(conta);
    }

    @Transactional
    public ContaResponse sacar(Long contaId, OperacaoRequest pedido) {
        Conta conta = bloqueada(contaId);
        exigirAtiva(conta);
        BigDecimal valor = normalizar(pedido.valor());
        if (!conta.possuiSaldoPara(valor)) {
            throw new RegraDeNegocioException("Saldo e limite insuficientes na conta \"" + conta.getNumero() + "\".");
        }
        conta.sacar(valor);
        contas.save(conta);
        registrar(conta, TipoMovimentacao.SAQUE, valor, descricaoOu(pedido.descricao(), "Saque em dinheiro"), null);
        return ContaResponse.de(conta);
    }

    @Transactional
    public TransferenciaResponse transferir(TransferenciaRequest pedido) {
        if (pedido.origemId().equals(pedido.destinoId())) {
            throw new RegraDeNegocioException("A conta de origem deve ser diferente da conta de destino.");
        }
        Long primeiroId = Math.min(pedido.origemId(), pedido.destinoId());
        Long segundoId = Math.max(pedido.origemId(), pedido.destinoId());
        Conta primeira = bloqueada(primeiroId);
        Conta segunda = bloqueada(segundoId);
        Conta origem = pedido.origemId().equals(primeira.getId()) ? primeira : segunda;
        Conta destino = pedido.destinoId().equals(primeira.getId()) ? primeira : segunda;

        exigirAtiva(origem);
        if (!destino.ativa()) {
            throw new RegraDeNegocioException(
                    "A conta de destino \"" + destino.getNumero() + "\" não está ativa para receber transferências.");
        }

        BigDecimal valor = normalizar(pedido.valor());
        if (!origem.possuiSaldoPara(valor)) {
            throw new RegraDeNegocioException("Saldo e limite insuficientes na conta \"" + origem.getNumero() + "\".");
        }

        String descricao = descricaoOu(pedido.descricao(), "Transferência entre contas");
        Instant agora = Instant.now();
        origem.sacar(valor);
        destino.depositar(valor);
        contas.save(origem);
        contas.save(destino);

        registrar(origem, TipoMovimentacao.TRANSFERENCIA_ENVIADA, valor, descricao,
                destino.getNumero() + " · " + destino.getTitular(), agora);
        registrar(destino, TipoMovimentacao.TRANSFERENCIA_RECEBIDA, valor, descricao,
                origem.getNumero() + " · " + origem.getTitular(), agora);

        return new TransferenciaResponse(
                origem.getNumero(),
                origem.getTitular(),
                destino.getNumero(),
                destino.getTitular(),
                valor,
                origem.getSaldo(),
                destino.getSaldo(),
                descricao,
                agora);
    }

    private Conta bloqueada(Long id) {
        return contas.buscarParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
    }

    private void exigirAtiva(Conta conta) {
        if (!conta.ativa()) {
            throw new RegraDeNegocioException(
                    "A conta \"" + conta.getNumero() + "\" não está ativa para movimentações.");
        }
    }

    private Movimentacao registrar(Conta conta, TipoMovimentacao tipo, BigDecimal valor,
                                   String descricao, String contraparte) {
        return registrar(conta, tipo, valor, descricao, contraparte, Instant.now());
    }

    private Movimentacao registrar(Conta conta, TipoMovimentacao tipo, BigDecimal valor,
                                   String descricao, String contraparte, Instant quando) {
        Movimentacao movimentacao = new Movimentacao(conta, tipo, valor, conta.getSaldo(), descricao, contraparte);
        movimentacao.setCriadaEm(quando);
        return movimentacoes.save(movimentacao);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String descricaoOu(String descricao, String padrao) {
        return descricao == null || descricao.isBlank() ? padrao : descricao.trim();
    }
}
