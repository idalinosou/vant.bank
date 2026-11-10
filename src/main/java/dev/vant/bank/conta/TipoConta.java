package dev.vant.bank.conta;

public enum TipoConta {

    CORRENTE("Corrente"),
    POUPANCA("Poupança");

    private final String rotulo;

    TipoConta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
