package cash.ird.webwallet.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WalletCommunicationException extends Exception{

    public WalletCommunicationException() {
        super("Error during communication with walletd!");
    }

    public WalletCommunicationException(String message) {
        super(message);
    }

}
