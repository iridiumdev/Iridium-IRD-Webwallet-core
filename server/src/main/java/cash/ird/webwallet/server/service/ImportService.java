package cash.ird.webwallet.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ImportService {

    private final WalletdContainerService walletdContainerService;

    @Autowired
    public ImportService(WalletdContainerService walletdContainerService) {
        this.walletdContainerService = walletdContainerService;
    }

    public Mono<String> importFromKeys(String viewSecretKey, String spendSecretKey) {


        return Mono.just("implement me!");
    }

}
