package dev.vant.bank.conta;

import dev.vant.bank.conta.dto.AtualizarContaRequest;
import dev.vant.bank.conta.dto.ContaRequest;
import dev.vant.bank.conta.dto.ContaResponse;
import dev.vant.bank.conta.dto.StatusContaRequest;
import dev.vant.bank.movimentacao.MovimentacaoService;
import dev.vant.bank.movimentacao.TipoMovimentacao;
import dev.vant.bank.movimentacao.dto.MovimentacaoResponse;
import dev.vant.bank.operacao.OperacaoRequest;
import dev.vant.bank.operacao.OperacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService contas;
    private final OperacaoService operacoes;
    private final MovimentacaoService movimentacoes;

    public ContaController(ContaService contas, OperacaoService operacoes, MovimentacaoService movimentacoes) {
        this.contas = contas;
        this.operacoes = operacoes;
        this.movimentacoes = movimentacoes;
    }

    @GetMapping
    public Page<ContaResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusConta status,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "8") int tamanho) {
        return contas.listar(busca, status, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public ContaResponse buscar(@PathVariable Long id) {
        return contas.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest pedido) {
        ContaResponse conta = contas.criar(pedido);
        return ResponseEntity.created(URI.create("/api/contas/" + conta.id())).body(conta);
    }

    @PutMapping("/{id}")
    public ContaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarContaRequest pedido) {
        return contas.atualizar(id, pedido);
    }

    @PatchMapping("/{id}/status")
    public ContaResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusContaRequest pedido) {
        return contas.alterarStatus(id, pedido.status());
    }

    @PostMapping("/{id}/depositos")
    public ContaResponse depositar(@PathVariable Long id, @Valid @RequestBody OperacaoRequest pedido) {
        return operacoes.depositar(id, pedido);
    }

    @PostMapping("/{id}/saques")
    public ContaResponse sacar(@PathVariable Long id, @Valid @RequestBody OperacaoRequest pedido) {
        return operacoes.sacar(id, pedido);
    }

    @GetMapping("/{id}/extrato")
    public Page<MovimentacaoResponse> extrato(
            @PathVariable Long id,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        return movimentacoes.extrato(id, tipo, pagina, tamanho);
    }
}
