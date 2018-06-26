package cash.ird.webwallet.server.config.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BearerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {

    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String, String> isolateBearerValue = authValue -> authValue.substring(BEARER.length());

    private final JWSVerifier jwsVerifier;

    public BearerAuthenticationConverter(JWSVerifier jwsVerifier) {
        this.jwsVerifier = jwsVerifier;
    }

    /**
     * This extracts and verifies the token form the header of the current {@link ServerWebExchange} to build a proper
     * {@link Authentication} instance
     *
     * @param serverWebExchange The current exchange to check the presence of the bearer token on
     * @return A proper filled {@link Authentication}  wrapped in a {@link Mono} or {@link Mono#empty()} if the token
     * is invalid or missing.
     */
    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange)
                .map(this::extractFromHeader)
                .filter(Objects::nonNull)
                .filter(matchBearerLength)
                .map(isolateBearerValue)
                .flatMap(this::applyPlain);
    }

    public Mono<Authentication> applyPlain(String rawToken) {
        return Mono.justOrEmpty(rawToken)
                .filter(Objects::nonNull)
                .filter(token -> !token.isEmpty())
                .map(this::checkSignedToken)
                .flatMap(this::createAuthentication)
                .filter(Objects::nonNull);
    }

    private String extractFromHeader(ServerWebExchange serverWebExchange) {
        return serverWebExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);
    }


    private Mono<SignedJWT> checkSignedToken(String token) {
        return Mono.create(sink -> {
            try {
                SignedJWT jwt = SignedJWT.parse(token);
                if (jwsVerifier.verify(jwt.getHeader(), jwt.getSigningInput(), jwt.getSignature())){
                    sink.success(jwt);
                } else {
                    sink.error(new BadCredentialsException("Token signature could not be verified!"));
                }
            } catch (ParseException | JOSEException e) {
                sink.error(e);
            }
        });
    }

    private Mono<Authentication> createAuthentication(Mono<SignedJWT> signedJWTMono) {
        return signedJWTMono
                .map(signedJWT -> {

                    String subject;
                    String auths;
                    List<GrantedAuthority> authorities;

                    try {
                        subject = signedJWT.getJWTClaimsSet().getSubject();
                        auths = (String) signedJWT.getJWTClaimsSet().getClaim("authorities");
                    } catch (ParseException e) {
                        return null;
                    }

                    if (auths != null) {
                        authorities = Stream.of(auths.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    } else {
                        authorities = Collections.emptyList();
                    }


                    return new UsernamePasswordAuthenticationToken(subject, null, authorities);

                });

    }
}