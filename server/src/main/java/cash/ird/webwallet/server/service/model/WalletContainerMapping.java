package cash.ird.webwallet.server.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class WalletContainerMapping {

    @NonNull
    private String address;

    @NonNull
    private String container;

}
