package cash.ird.webwallet.server.rest.model;

import lombok.Data;

@Data
public class KeyImport {

    private String viewSecretKey;
    private String spendSecretKey;
    private String password;

}
