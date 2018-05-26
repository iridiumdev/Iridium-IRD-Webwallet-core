package cash.ird.webwallet.server.config.security;

import cash.ird.webwallet.server.rest.model.TokenPairDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

/**
 * On success authentication a signed JWT object is serialized and added
 * in the authorization header as a bearer token
 */
public class JWTAuthenticationSuccessHandler
        implements ServerAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    public JWTAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response
                .writeWith(Mono.create(sink -> {
                    // TODO: 26.05.18 add refresh token and fix those nasty hardcoded values here and there.
                    TokenPairDto tokenPairDto = new TokenPairDto().setAccessToken(
                            new JWTTokenService().generateToken(
                                    authentication.getName(),
                                    authentication.getCredentials(),
                                    authentication.getAuthorities())
                    );

                    DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
                    try {

                        sink.success(bufferFactory.wrap(objectMapper.writeValueAsBytes(tokenPairDto)));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }));

    }

}