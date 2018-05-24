package cash.ird.webwallet.server.service;

import cash.ird.webwallet.server.domain.User;
import cash.ird.webwallet.server.domain.Wallet;
import cash.ird.webwallet.server.exception.WalletAlreadyImportedException;
import cash.ird.webwallet.server.exception.WalletCommunicationException;
import cash.ird.webwallet.server.exception.WalletNotImportedException;
import cash.ird.webwallet.server.repository.WalletRepository;
import cash.ird.webwallet.server.rest.model.WalletContainerDto;
import cash.ird.webwallet.server.rest.model.WalletStatusDto;
import cash.ird.webwallet.server.service.model.WalletContainerInstance;
import cash.ird.webwallet.server.service.walletd.ReactiveIridiumAPI;
import cash.ird.webwallet.server.service.walletd.WalletdDispatcherClient;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class WalletService {

    private final WalletdContainerService walletdContainerService;
    private final WalletdDispatcherClient dispatcherClient;
    private final WalletRepository walletRepository;

    @Autowired
    public WalletService(WalletdContainerService walletdContainerService, WalletdDispatcherClient dispatcherClient, WalletRepository walletRepository) {
        this.walletdContainerService = walletdContainerService;
        this.dispatcherClient = dispatcherClient;
        this.walletRepository = walletRepository;
    }

    public Mono<Wallet> importFromKeys(User user, String viewSecretKey, String spendSecretKey, String password) {

        return generateWalletHash(viewSecretKey, spendSecretKey)
                .flatMap(hash -> Mono.justOrEmpty(walletRepository.findByContainerHash(hash))
                            .flatMap(wallet -> Mono.<WalletContainerInstance>error(new  WalletAlreadyImportedException()))
                            .switchIfEmpty(walletdContainerService.createWalletContainer(hash, viewSecretKey, spendSecretKey, password))
                            .flatMap(instance -> Mono.just(walletRepository.save(Wallet.of(instance.getAddress(), user.getUsername(), instance.getContainerHash()))))
                );

    }

    public Mono<Wallet> fetchWalletForUser(String address, User user){
        return Mono.justOrEmpty(walletRepository.findByAddressAndUsername(address, user.getUsername()))
                .switchIfEmpty(Mono.error(new WalletNotImportedException()));
    }


    public Flux<Wallet> fetchAllWalletsForUser(User user){
        return Flux.fromIterable(walletRepository.findAllByUsername(user.getUsername()));
    }

    public Mono<WalletContainerDto> loadWallet(Wallet wallet, String password) {
        return walletdContainerService.loadWalletContainer(wallet, password);
    }

    public Mono<WalletStatusDto> getStatus(Wallet wallet) {
        return Mono.just(wallet)
                .flatMap(walletdContainerService::checkWalletLoaded)
                .map(checkedWallet -> {
                    WalletStatusDto walletStatus = new WalletStatusDto();
                    walletStatus.setAddress(checkedWallet.getAddress());
                    walletStatus.setContainerName(checkedWallet.getContainerName());
                    return walletStatus;
                })
                .flatMap(this::queryWalletStatus)
                .flatMap(this::queryWalletBalance);
    }


    private Mono<ReactiveIridiumAPI> connectApi(String target) {
        return dispatcherClient.reactiveTarget(target);
    }

    private Mono<WalletStatusDto> queryWalletStatus(WalletStatusDto walletStatus){

        return connectApi(walletStatus.getContainerName())
                .flatMap(ReactiveIridiumAPI::getStatus)
                .map(status -> {

                    if (status.getBlockCount() < status.getKnownBlockCount()) {
                        walletStatus.setIridiumStatus(WalletStatusDto.BlockchainStatus.SYNCING);
                    } else {
                        walletStatus.setIridiumStatus(WalletStatusDto.BlockchainStatus.SYNCED);
                    }

                    walletStatus.setCurrentBlockHeight(status.getBlockCount());
                    walletStatus.setNetworkBlockHeight(status.getKnownBlockCount());

                    walletStatus.setContainerStatus(WalletStatusDto.ContainerStatus.RUNNING);
                    return walletStatus;
                })
                .switchIfEmpty(Mono.error(new WalletCommunicationException()));

    }

    private Mono<WalletStatusDto> queryWalletBalance(WalletStatusDto walletStatus){

        return connectApi(walletStatus.getContainerName())
                .flatMap(ReactiveIridiumAPI::getBalance)
                .map(balance -> {
                    walletStatus.setCurrentBalance(balance.getAvailableBalance());
                    walletStatus.setLockedBalance(balance.getLockedAmount());
                    return walletStatus;
                })
                .switchIfEmpty(Mono.error(new WalletCommunicationException()));

    }

    private Mono<String> generateWalletHash(String viewSecretKey, String spendSecretKey){

        //noinspection UnstableApiUsage
        return Mono.create(sink -> sink.success(Hashing.sha256()
                .hashString(viewSecretKey + spendSecretKey, StandardCharsets.UTF_8)
                .toString()));

    }




}
