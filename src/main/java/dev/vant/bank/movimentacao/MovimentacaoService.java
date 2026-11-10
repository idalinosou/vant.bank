package dev.vant.bank.movimentacao;

import dev.vant.bank.conta.ContaRepository;
import dev.vant.bank.erro.RecursoNaoEncontradoException;
import dev.vant.bank.movimentacao.dto.MovimentacaoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimentacaoService {

    private final ContaRepository contas;
    private final MovimentacaoRepository movimentacoes;

    public MovimentacaoService(ContaRepository contas, MovimentacaoRepository movimentacoes) {
        this.contas = contas;
        this.movimentacoes = movimentacoes;
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> extrato(Long contaId, TipoMovimentacao tipo, int pagina, int tamanho) {
        if (!contas.existsById(contaId)) {
            throw new RecursoNaoEncontradoException("Conta não encontrada.");
        }
        PageRequest paginacao = PageRequest.of(
                Math.max(pagina, 1) - 1,
                Math.min(Math.max(tamanho, 1), 50));
        Page<Movimentacao> resultado = tipo == null
                ? movimentacoes.findByContaIdOrderByCriadaEmDesc(contaId, paginacao)
                : movimentacoes.findByContaIdAndTipoOrderByCriadaEmDesc(contaId, tipo, paginacao);
        return resultado.map(MovimentacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> ultimas() {
        return movimentacoes.findTop6ByOrderByCriadaEmDesc().stream()
                .map(MovimentacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> transferenciasRecentes() {
        return movimentacoes.findTop8ByTipoOrderByCriadaEmDesc(TipoMovimentacao.TRANSFERENCIA_ENVIADA).stream()
                .map(MovimentacaoResponse::de)
                .toList();
    }
}
