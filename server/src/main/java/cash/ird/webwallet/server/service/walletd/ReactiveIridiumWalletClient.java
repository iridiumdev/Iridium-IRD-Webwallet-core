package cash.ird.webwallet.server.service.walletd;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.model.body.Balance;
import cash.ird.walletd.model.body.Status;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

public class ReactiveIridiumWalletClient implements ReactiveIridiumAPI {

    private final IridiumAPI api;

    public ReactiveIridiumWalletClient(IridiumAPI api) {
        this.api = api;
    }

    @Override
    public Mono<Status> getStatus() {
        return async(api::getStatus);
    }

    @Override
    public Mono<Balance> getBalance() {
        return async(api::getBalance);
    }

    private <T> Mono<T> async(Callable<T> callable) {
        return Mono.fromCallable(callable).publishOn(Schedulers.parallel());
    }

}
