package cash.ird.webwallet.server.rest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KeyImportDto extends PasswordDto{

    private String viewSecretKey;
    private String spendSecretKey;

}
