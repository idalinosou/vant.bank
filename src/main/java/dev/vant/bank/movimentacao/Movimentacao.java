package dev.vant.bank.movimentacao;

import dev.vant.bank.conta.Conta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(length = 160)
    private String descricao;

    @Column(length = 80)
    private String contraparte;

    @Column(nullable = false)
    private Instant criadaEm;

    public Movimentacao() {
    }

    public Movimentacao(Conta conta, TipoMovimentacao tipo, BigDecimal valor,
                        BigDecimal saldoPosterior, String descricao, String contraparte) {
        this.conta = conta;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoPosterior = saldoPosterior;
        this.descricao = descricao;
        this.contraparte = contraparte;
    }

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getContraparte() {
        return contraparte;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Instant criadaEm) {
        this.criadaEm = criadaEm;
    }
}
