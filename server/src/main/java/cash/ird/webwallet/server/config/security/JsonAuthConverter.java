package cash.ird.webwallet.server.config.security;

import cash.ird.webwallet.server.rest.model.AuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class JsonAuthConverter implements Function<ServerWebExchange, Mono<Authentication>> {

    private final BearerAuthenticationConverter bearerAuthenticationConverter;
    private final PathPatternParserServerWebExchangeMatcher matcher = new PathPatternParserServerWebExchangeMatcher("/auth/token", HttpMethod.POST);

    public JsonAuthConverter(BearerAuthenticationConverter bearerAuthenticationConverter) {
        this.bearerAuthenticationConverter = bearerAuthenticationConverter;
    }

    @Override
    public Mono<Authentication> apply(ServerWebExchange exchange) {

//        StringDecoder stringDecoder = new StringDecoder(StringDecoder.DEFAULT_DELIMITERS);

        BodyExtractor<Mono<AuthDto>, ReactiveHttpInputMessage> extractor = BodyExtractors.toMono(AuthDto.class);

        BodyExtractor.Context context = new BodyExtractor.Context() {
            @Override
            public List<HttpMessageReader<?>> messageReaders() {
                return Collections.singletonList(
                        new DecoderHttpMessageReader(new Jackson2JsonDecoder())
                );
            }

            @Override
            public Optional<ServerHttpResponse> serverResponse() {
                return Optional.empty();
            }

            @Override
            public Map<String, Object> hints() {
                return Collections.emptyMap();
            }
        };

        return matcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .filter(matchResult -> exchange.getRequest().getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))
                .map(matchResult -> exchange.getRequest().getBody())
                .flatMap((Flux<DataBuffer> dataBuffer) -> extractor.extract(exchange.getRequest(), context))
                .flatMap(authDto -> {
                    if (authDto.getGrantType() == AuthDto.GrantType.PASSWORD) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(authDto.getUsername(), authDto.getPassword());
                        auth.setAuthenticated(false);
                        return Mono.just(auth);
                    } else if (authDto.getGrantType() == AuthDto.GrantType.REFRESH_TOKEN) {
                        return this.bearerAuthenticationConverter.applyPlain(authDto.getRefreshToken());
                    }
                    return Mono.empty();
                });

    }

}
