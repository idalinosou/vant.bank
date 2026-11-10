package dev.vant.bank.erro;

import java.time.Instant;
import java.util.Map;

public record ErroResponse(Instant instante, int status, String mensagem, String caminho, Map<String, String> campos) {
}
