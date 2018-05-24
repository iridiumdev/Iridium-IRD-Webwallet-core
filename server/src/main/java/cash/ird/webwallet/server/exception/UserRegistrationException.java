package cash.ird.webwallet.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserRegistrationException extends Exception{

    public UserRegistrationException(String message) {
        super(message);
    }

}
