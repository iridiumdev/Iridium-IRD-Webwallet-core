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

import java.text.ParseException;

/**
 * On success authentication a signed JWT pair is serialized in the body
 */
public class JWTSuccessHandler
        implements ServerAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JWTTokenService jwtTokenService;

    public JWTSuccessHandler(ObjectMapper objectMapper, JWTTokenService jwtTokenService) {
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
    }


    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {

        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response
                .writeWith(Mono.create(sink -> {
                    TokenPairDto tokenPairDto;
                    try {

                        tokenPairDto = jwtTokenService.generateTokenPair(authentication.getName(), authentication.getAuthorities());

                        DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

                        sink.success(bufferFactory.wrap(objectMapper.writeValueAsBytes(tokenPairDto)));
                    } catch (JsonProcessingException | ParseException e) {
                        sink.error(e);
                    }
                }));

    }

}