package cash.ird.webwallet.server.rest.controller;

import cash.ird.webwallet.server.rest.model.KeyImport;
import cash.ird.webwallet.server.rest.model.WalletBase;
import cash.ird.webwallet.server.rest.model.WalletStatus;
import cash.ird.webwallet.server.service.WalletService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class WalletController {


    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/api/wallet/import")
    public Mono<WalletBase> importFromKeys(@RequestBody KeyImport keyImport) {

        return walletService.importFromKeys(keyImport.getViewSecretKey(), keyImport.getSpendSecretKey(), keyImport.getPassword())
                .map(WalletBase::of);

    }

    @GetMapping("/api/wallet/{address}/status")
    public Mono<WalletStatus> getStatus(@PathVariable("address") String address, @RequestParam("password") String password) {
        WalletBase wallet = new WalletBase();
        wallet.setAddress(address);
        wallet.setPassword(password);

        return walletService.getStatus(wallet);

    }


}
