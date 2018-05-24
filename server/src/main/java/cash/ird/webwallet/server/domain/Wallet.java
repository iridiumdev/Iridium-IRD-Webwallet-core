package cash.ird.webwallet.server.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;


@RedisHash("wallet")
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class Wallet{


    @Id
    private String id;

    @Indexed
    @NonNull
    private String address;

    @Indexed
    @NonNull
    private String username;

    @NonNull
    private String containerHash;

    public String getContainerName() {
        return String.format("%s.wallet", this.containerHash);
    }

    public String getContainerVolumeName() {
        return getContainerName();
    }

}
