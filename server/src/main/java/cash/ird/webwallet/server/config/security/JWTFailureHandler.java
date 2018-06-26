package cash.ird.webwallet.server.config.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class JWTFailureHandler implements ServerAuthenticationFailureHandler {

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage(), exception));
    }

}
