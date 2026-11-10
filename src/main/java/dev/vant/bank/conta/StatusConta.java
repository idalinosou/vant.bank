package dev.vant.bank.conta;

import java.util.EnumSet;
import java.util.Set;

public enum StatusConta {

    ATIVA("Ativa"),
    BLOQUEADA("Bloqueada"),
    ENCERRADA("Encerrada");

    private final String rotulo;

    StatusConta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean podeMudarPara(StatusConta destino) {
        Set<StatusConta> permitidos = switch (this) {
            case ATIVA -> EnumSet.of(BLOQUEADA, ENCERRADA);
            case BLOQUEADA -> EnumSet.of(ATIVA, ENCERRADA);
            case ENCERRADA -> EnumSet.noneOf(StatusConta.class);
        };
        return permitidos.contains(destino);
    }
}
