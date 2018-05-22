package cash.ird.webwallet.server.rest.controller;

import cash.ird.walletd.rpc.exception.IridiumWalletdException;
import cash.ird.webwallet.server.rest.model.KeyImport;
import cash.ird.webwallet.server.service.WalletdContainerService;
import com.spotify.docker.client.exceptions.DockerException;
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
    public Mono<String> importKeys(@RequestBody KeyImport keyImport) {

        // TODO: 22.05.18 - proper error handling...
        try {
            return walletdContainerService.createWallet(keyImport.getViewSecretKey(), keyImport.getSpendSecretKey(), keyImport.getPassword());
        } catch (DockerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IridiumWalletdException e) {
            e.printStackTrace();
        }
        return null;
    }


}
