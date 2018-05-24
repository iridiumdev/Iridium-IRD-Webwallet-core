package cash.ird.webwallet.server.rest.controller;

import cash.ird.webwallet.server.domain.User;
import cash.ird.webwallet.server.rest.CurrentUser;
import cash.ird.webwallet.server.rest.model.KeyImportDto;
import cash.ird.webwallet.server.rest.model.WalletBaseDto;
import cash.ird.webwallet.server.rest.model.WalletContainerDto;
import cash.ird.webwallet.server.rest.model.WalletStatusDto;
import cash.ird.webwallet.server.service.WalletService;
import cash.ird.webwallet.server.service.WalletdContainerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@PreAuthorize("isAuthenticated()")
public class WalletController {


    private final WalletService walletService;
    private final WalletdContainerService walletdContainerService;

    public WalletController(WalletService walletService, WalletdContainerService walletdContainerService) {
        this.walletService = walletService;
        this.walletdContainerService = walletdContainerService;
    }

    @PostMapping("/api/wallets")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WalletBaseDto> importFromKeys(@CurrentUser User user, @RequestBody KeyImportDto keyImportDto) {
        return walletService.importFromKeys(user, keyImportDto.getViewSecretKey(), keyImportDto.getSpendSecretKey(), keyImportDto.getPassword())
                .map(w -> WalletBaseDto.of(w.getAddress()));

    }

    @GetMapping("/api/wallets")
    public Flux<WalletContainerDto> listWallets(@CurrentUser User user) {
        return walletService.fetchAllWalletsForUser(user)
                .flatMap(walletdContainerService::getWalletContainerStatus);
    }


    @PostMapping("/api/wallets/{address}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WalletContainerDto> loadWallet(@CurrentUser User user, @PathVariable("address") String address, @RequestBody WalletBaseDto walletBaseDto) {
        return walletService.fetchWalletForUser(address, user)
                .flatMap(wallet -> walletService.loadWallet(wallet, walletBaseDto.getPassword()));
    }

    @GetMapping("/api/wallets/{address}")
    public Mono<WalletStatusDto> getStatus(@CurrentUser User user, @PathVariable("address") String address) {
        return walletService.fetchWalletForUser(address, user)
                .flatMap(walletService::getStatus);
    }


}
