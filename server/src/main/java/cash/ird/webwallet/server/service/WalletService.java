package cash.ird.webwallet.server.service;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.model.body.Status;
import cash.ird.walletd.rpc.exception.IridiumWalletdException;
import cash.ird.webwallet.server.rest.model.WalletBase;
import cash.ird.webwallet.server.rest.model.WalletStatus;
import cash.ird.webwallet.server.service.walletd.WalletdDispatcherClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WalletService {

    private final WalletdContainerService walletdContainerService;
    private final WalletdDispatcherClient dispatcherClient;

    @Autowired
    public WalletService(WalletdContainerService walletdContainerService, WalletdDispatcherClient dispatcherClient) {
        this.walletdContainerService = walletdContainerService;
        this.dispatcherClient = dispatcherClient;
    }

    public Mono<String> importFromKeys(String viewSecretKey, String spendSecretKey, String password) {
        return walletdContainerService.createWallet(viewSecretKey, spendSecretKey, password);
    }

    public Mono<WalletStatus> getStatus(WalletBase wallet) {

        return Mono.create(sink -> walletdContainerService.loadWallet(wallet.getAddress(), wallet.getPassword())
                .subscribe(instance -> {
                    WalletStatus walletStatus = new WalletStatus();
                    walletStatus.setAddress(wallet.getAddress());
                    try {
                        IridiumAPI api = dispatcherClient.target(instance.getContainer());

                        Status status = api.getStatus();

                        if (status.getBlockCount() < status.getKnownBlockCount()){
                            walletStatus.setIridiumStatus(WalletStatus.BlockchainStatus.SYNCING);
                        } else {
                            walletStatus.setIridiumStatus(WalletStatus.BlockchainStatus.SYNCED);
                        }

                        walletStatus.setContainerStatus(WalletStatus.ContainerStatus.RUNNING);

                        sink.success(walletStatus);

                    } catch (IridiumWalletdException e) {
                        log.debug("Error while retrieving status for address {}. Exception is: ", wallet.getAddress(), e);
                        walletStatus.setIridiumStatus(WalletStatus.BlockchainStatus.ERROR);
                        walletStatus.setContainerStatus(WalletStatus.ContainerStatus.ERROR);
                        sink.success(walletStatus);
                    }
                }));

    }


}
