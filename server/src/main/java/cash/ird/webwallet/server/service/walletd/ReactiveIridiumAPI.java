package cash.ird.webwallet.server.service.walletd;

import cash.ird.walletd.model.body.Balance;
import cash.ird.walletd.model.body.Status;
import cash.ird.walletd.rpc.exception.IridiumWalletdException;
import reactor.core.publisher.Mono;

public interface ReactiveIridiumAPI {

    Mono<Status> getStatus();

    Mono<Balance> getBalance();

}
