package dev.vant.bank.conta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false, length = 140)
    private String titular;

    @Column(length = 30)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo = TipoConta.CORRENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status = StatusConta.ATIVA;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limite = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant criadaEm;

    public Conta() {
    }

    public Conta(String numero, String titular, String documento, TipoConta tipo, BigDecimal limite) {
        this.numero = numero;
        this.titular = titular;
        this.documento = documento;
        this.tipo = tipo;
        this.limite = limite;
    }

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
    }

    public void atualizar(String titular, String documento, TipoConta tipo, BigDecimal limite) {
        this.titular = titular;
        this.documento = documento;
        this.tipo = tipo;
        this.limite = limite;
    }

    public void depositar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void sacar(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
    }

    public boolean possuiSaldoPara(BigDecimal valor) {
        return saldo.add(limite).compareTo(valor) >= 0;
    }

    public boolean ativa() {
        return status == StatusConta.ATIVA;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public String getTitular() {
        return titular;
    }

    public String getDocumento() {
        return documento;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public StatusConta getStatus() {
        return status;
    }

    public void setStatus(StatusConta status) {
        this.status = status;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(Instant criadaEm) {
        this.criadaEm = criadaEm;
    }
}
