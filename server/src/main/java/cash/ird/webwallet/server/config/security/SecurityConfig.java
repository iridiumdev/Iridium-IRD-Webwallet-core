package cash.ird.webwallet.server.config.security;

import cash.ird.webwallet.server.config.props.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ReactiveUserDetailsService userDetailsRepository;

    @Autowired
    public SecurityConfig(ReactiveUserDetailsService userService) {
        this.userDetailsRepository = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        String idForEncode = "bcrypt";

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
        encoders.put("scrypt", new SCryptPasswordEncoder());

       return new DelegatingPasswordEncoder(idForEncode, encoders);
    }


    @Bean
    public JWSSigner jwsSigner(JwtProperties jwtProperties) throws KeyLengthException {
        return new MACSigner(jwtProperties.getSecret());
    }
    @Bean
    public JWSVerifier jwsVerifier(JwtProperties jwtProperties) throws JOSEException {
        return new MACVerifier(jwtProperties.getSecret());
    }

    @Bean
    public ReactiveAuthenticationManager delegatingReactiveAuthenticationManager(){
        return new DelegatingReactiveAuthenticationManager(
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsRepository),
                new TrustedUserDetailsReactiveAuthenticationManager(userDetailsRepository)
        );
    }

    /**
     * For Spring Security webflux, a chain of filters will provide user authentication
     * and authorization, we add custom filters to enable JWT token approach.
     *
     * @param http An initial object to build common filter scenarios.
     *             Customized filters are added here.
     * @return SecurityWebFilterChain A filter chain for web exchanges that will
     * provide security
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ObjectMapper objectMapper, JWTTokenService jwtTokenService, JWSVerifier jwsVerifier, ReactiveAuthenticationManager delegatingReactiveAuthenticationManager) {

        BearerAuthenticationConverter bearerAuthenticationConverter = new BearerAuthenticationConverter(jwsVerifier);

        // For the auth with username+password over json or using the refresh_token -> get a new pair of tokens
        AuthenticationWebFilter jwtAuthenticationFilter = new AuthenticationWebFilter(delegatingReactiveAuthenticationManager);
        jwtAuthenticationFilter.setAuthenticationSuccessHandler(new JWTSuccessHandler(objectMapper, jwtTokenService));
        jwtAuthenticationFilter.setAuthenticationFailureHandler(new JWTFailureHandler());
        jwtAuthenticationFilter.setAuthenticationConverter(new JsonAuthConverter(bearerAuthenticationConverter));
        jwtAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/auth/token"));


        JWTAuthorizationWebFilter jwtAuthorizationFilter = new JWTAuthorizationWebFilter(bearerAuthenticationConverter);


        http
                .addFilterAt((exchange, chain) -> chain.filter(new CacheServerWebExchangeDecorator(exchange)), SecurityWebFiltersOrder.FIRST)
                .authorizeExchange()
                .pathMatchers("/**", "/auth/token", "/auth/register")
                .permitAll()
                .and()
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.FIRST)
                .authorizeExchange()
                .pathMatchers("/api/**")
                .authenticated()
                .and()
                .addFilterAt(jwtAuthorizationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
                .csrf().disable()
                ;

        return http.build();
    }

}
