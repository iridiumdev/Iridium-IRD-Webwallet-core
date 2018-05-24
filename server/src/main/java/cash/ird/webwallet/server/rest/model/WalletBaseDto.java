package cash.ird.webwallet.server.rest.model;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class WalletBaseDto extends PasswordDto {

    @NonNull
    private String address;

}
