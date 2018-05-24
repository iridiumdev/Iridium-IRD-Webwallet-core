package cash.ird.webwallet.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WalletNotLoadedException extends Exception{

    public WalletNotLoadedException(String message) {
        super(message);
    }

}
