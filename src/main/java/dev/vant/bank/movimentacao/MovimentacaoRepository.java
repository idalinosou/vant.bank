package dev.vant.bank.movimentacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    Page<Movimentacao> findByContaIdOrderByCriadaEmDesc(Long contaId, Pageable pageable);

    Page<Movimentacao> findByContaIdAndTipoOrderByCriadaEmDesc(Long contaId, TipoMovimentacao tipo, Pageable pageable);

    List<Movimentacao> findTop6ByOrderByCriadaEmDesc();

    List<Movimentacao> findTop8ByTipoOrderByCriadaEmDesc(TipoMovimentacao tipo);

    @Query("select coalesce(sum(m.valor), 0) from Movimentacao m where m.criadaEm >= :inicio and m.criadaEm < :fim")
    BigDecimal somarVolume(@Param("inicio") Instant inicio, @Param("fim") Instant fim);

    long countByTipoAndCriadaEmGreaterThanEqualAndCriadaEmLessThan(
            TipoMovimentacao tipo, Instant inicio, Instant fim);
}
