package cash.ird.webwallet.server.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@ToString(exclude = "password")
public class WalletBase {

    @NonNull
    private String address;

    @Getter(onMethod = @__({@JsonIgnore}))
    private String password;

}
