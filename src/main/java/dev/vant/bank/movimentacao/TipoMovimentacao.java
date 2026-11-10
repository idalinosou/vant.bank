package dev.vant.bank.movimentacao;

public enum TipoMovimentacao {

    DEPOSITO("Depósito", true),
    SAQUE("Saque", false),
    TRANSFERENCIA_ENVIADA("Transferência enviada", false),
    TRANSFERENCIA_RECEBIDA("Transferência recebida", true);

    private final String rotulo;
    private final boolean entrada;

    TipoMovimentacao(String rotulo, boolean entrada) {
        this.rotulo = rotulo;
        this.entrada = entrada;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isEntrada() {
        return entrada;
    }
}
