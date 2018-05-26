package cash.ird.webwallet.server.config.security;

import cash.ird.webwallet.server.rest.model.AuthDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
public class ServerJsonAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {

    private final ObjectMapper objectMapper;
    private final PathPatternParserServerWebExchangeMatcher matcher = new PathPatternParserServerWebExchangeMatcher("/auth/token", HttpMethod.POST);

    public ServerJsonAuthenticationConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange exchange) {


        return matcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .filter(matchResult -> exchange.getRequest().getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))
                .flatMap(matchResult -> exchange.getRequest().getBody().flatMap(dataBuffer -> {
                    try {
                        AuthDto authDto = objectMapper.readValue(dataBuffer.asInputStream(), AuthDto.class);
                        if (authDto.getGrantType().equals("password")) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(authDto.getUsername(), authDto.getPassword());
                            return Mono.just(auth);
                        }
                    } catch (IOException e) {
                        log.debug("Could not read json from login request body!", e);
                    }
                    return Mono.empty();
                }).next());

    }

}
