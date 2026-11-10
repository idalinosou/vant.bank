package dev.vant.bank.painel;

import dev.vant.bank.painel.dto.PainelResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService painel;

    public PainelController(PainelService painel) {
        this.painel = painel;
    }

    @GetMapping
    public PainelResponse resumo() {
        return painel.resumo();
    }
}
