package dev.vant.bank.transferencia;

import dev.vant.bank.movimentacao.MovimentacaoService;
import dev.vant.bank.movimentacao.dto.MovimentacaoResponse;
import dev.vant.bank.operacao.OperacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

    private final OperacaoService operacoes;
    private final MovimentacaoService movimentacoes;

    public TransferenciaController(OperacaoService operacoes, MovimentacaoService movimentacoes) {
        this.operacoes = operacoes;
        this.movimentacoes = movimentacoes;
    }

    @PostMapping
    public TransferenciaResponse transferir(@Valid @RequestBody TransferenciaRequest pedido) {
        return operacoes.transferir(pedido);
    }

    @GetMapping("/recentes")
    public List<MovimentacaoResponse> recentes() {
        return movimentacoes.transferenciasRecentes();
    }
}
