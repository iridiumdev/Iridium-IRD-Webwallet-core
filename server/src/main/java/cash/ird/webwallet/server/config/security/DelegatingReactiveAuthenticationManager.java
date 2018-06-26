package cash.ird.webwallet.server.config.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public class DelegatingReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveAuthenticationManager userDetailsManager;
    private final ReactiveAuthenticationManager trustedManager;

    public DelegatingReactiveAuthenticationManager(ReactiveAuthenticationManager userDetailsManager, ReactiveAuthenticationManager trustedManager) {
        this.userDetailsManager = userDetailsManager;
        this.trustedManager = trustedManager;
    }


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .flatMap(auth -> (auth.isAuthenticated()) ? trustedManager.authenticate(auth) : userDetailsManager.authenticate(auth));
    }
}
