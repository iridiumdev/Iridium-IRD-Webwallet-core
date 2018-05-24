package cash.ird.webwallet.server.repository;

import cash.ird.webwallet.server.domain.Wallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, String> {

    Optional<Wallet> findByContainerHash(String containerHash);

    Optional<Wallet> findByAddressAndUsername(String address, String username);

    Iterable<Wallet> findAllByUsername(String username);


}
