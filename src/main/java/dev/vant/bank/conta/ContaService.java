package dev.vant.bank.conta;

import dev.vant.bank.conta.dto.AtualizarContaRequest;
import dev.vant.bank.conta.dto.ContaRequest;
import dev.vant.bank.conta.dto.ContaResponse;
import dev.vant.bank.erro.RecursoNaoEncontradoException;
import dev.vant.bank.erro.RegraDeNegocioException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContaService {

    private final ContaRepository contas;

    public ContaService(ContaRepository contas) {
        this.contas = contas;
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(String busca, StatusConta status, int pagina, int tamanho) {
        Specification<Conta> filtro = (raiz, consulta, criterios) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (busca != null && !busca.isBlank()) {
                String termo = "%" + busca.trim().toLowerCase() + "%";
                predicados.add(criterios.or(
                        criterios.like(criterios.lower(raiz.get("numero")), termo),
                        criterios.like(criterios.lower(raiz.get("titular")), termo),
                        criterios.like(criterios.lower(raiz.get("documento")), termo)));
            }
            if (status != null) {
                predicados.add(criterios.equal(raiz.get("status"), status));
            }
            return criterios.and(predicados.toArray(Predicate[]::new));
        };
        PageRequest paginacao = PageRequest.of(
                Math.max(pagina, 1) - 1,
                Math.min(Math.max(tamanho, 1), 50),
                Sort.by("numero").ascending());
        return contas.findAll(filtro, paginacao).map(ContaResponse::de);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id) {
        return ContaResponse.de(entidade(id));
    }

    @Transactional
    public ContaResponse criar(ContaRequest pedido) {
        String numero = pedido.numero().trim();
        if (contas.existsByNumeroIgnoreCase(numero)) {
            throw new RegraDeNegocioException("Já existe uma conta com o número \"" + numero + "\".");
        }
        Conta conta = new Conta(
                numero,
                pedido.titular().trim(),
                texto(pedido.documento()),
                pedido.tipo(),
                limite(pedido.tipo(), pedido.limite()));
        return ContaResponse.de(contas.save(conta));
    }

    @Transactional
    public ContaResponse atualizar(Long id, AtualizarContaRequest pedido) {
        Conta conta = entidade(id);
        if (conta.getStatus() == StatusConta.ENCERRADA) {
            throw new RegraDeNegocioException("Conta encerrada não pode ser editada.");
        }
        conta.atualizar(
                pedido.titular().trim(),
                texto(pedido.documento()),
                pedido.tipo(),
                limite(pedido.tipo(), pedido.limite()));
        return ContaResponse.de(contas.save(conta));
    }

    @Transactional
    public ContaResponse alterarStatus(Long id, StatusConta novo) {
        Conta conta = entidade(id);
        if (conta.getStatus() == novo) {
            return ContaResponse.de(conta);
        }
        if (!conta.getStatus().podeMudarPara(novo)) {
            throw new RegraDeNegocioException(
                    "Não é possível alterar o status da conta de \"" + conta.getStatus().getRotulo()
                            + "\" para \"" + novo.getRotulo() + "\".");
        }
        if (novo == StatusConta.ENCERRADA && conta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new RegraDeNegocioException("A conta só pode ser encerrada com o saldo zerado.");
        }
        conta.setStatus(novo);
        return ContaResponse.de(contas.save(conta));
    }

    public Conta entidade(Long id) {
        return contas.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
    }

    private BigDecimal limite(TipoConta tipo, BigDecimal limite) {
        if (tipo == TipoConta.POUPANCA) {
            return BigDecimal.ZERO.setScale(2);
        }
        return limite == null ? BigDecimal.ZERO.setScale(2) : limite;
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
