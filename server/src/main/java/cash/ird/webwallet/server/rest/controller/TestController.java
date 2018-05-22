package cash.ird.webwallet.server.rest.controller;

import cash.ird.webwallet.server.rest.model.KeyImport;
import cash.ird.webwallet.server.service.WalletdContainerService;
import cash.ird.webwallet.server.service.model.WalletContainerMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {


    private final WalletdContainerService walletdContainerService;

    public TestController(WalletdContainerService walletdContainerService) {
        this.walletdContainerService = walletdContainerService;
    }

    @PostMapping("/import")
    public Mono<WalletContainerMapping> importKeys(@RequestBody KeyImport keyImport) {

        return walletdContainerService.createWallet(keyImport.getViewSecretKey(), keyImport.getSpendSecretKey(), keyImport.getPassword());

    }


}
