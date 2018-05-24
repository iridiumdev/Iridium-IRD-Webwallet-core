package cash.ird.webwallet.server.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class WalletContainerDto extends WalletBaseDto {

    private ContainerStatus containerStatus;

    private String containerName;

    public enum ContainerStatus {
        DELETED,
        RUNNING,
        ERROR
    }
}
