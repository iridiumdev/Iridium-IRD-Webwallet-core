package cash.ird.webwallet.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WalletAlreadyImportedException extends Exception{

    public WalletAlreadyImportedException() {
        super("Wallet is already imported!");
    }

    public WalletAlreadyImportedException(String message) {
        super(message);
    }

}
