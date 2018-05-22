package cash.ird.webwallet.server.rest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class WalletStatus extends WalletBase {

    private BlockchainStatus iridiumStatus;

    private ContainerStatus containerStatus;

    public static enum BlockchainStatus {
        SYNCING,
        SYNCED,
        ERROR,
        UNKNOWN
    }

    public static enum ContainerStatus {
        DELETED,
        RUNNING,
        ERROR
    }

}
