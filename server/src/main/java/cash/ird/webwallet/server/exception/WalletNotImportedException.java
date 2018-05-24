package cash.ird.webwallet.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WalletNotImportedException extends Exception{

    public WalletNotImportedException() {
        super("Wallet does not exist!");
    }

    public WalletNotImportedException(String message) {
        super(message);
    }

}
