package cash.ird.webwallet.server.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletStatusDto extends WalletContainerDto {

    private Long currentBlockHeight;
    private Long networkBlockHeight;

    private Long currentBalance;
    private Long lockedBalance;

    private BlockchainStatus iridiumStatus;

    public enum BlockchainStatus {
        SYNCING,
        SYNCED,
        ERROR,
        UNKNOWN
    }


}
