package cash.ird.webwallet.server.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;


@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.resource.id}")
    private String resourceId;

    private final DefaultTokenServices tokenServices;
    private final TokenStore tokenStore;

    @Autowired
    public ResourceServerConfig(DefaultTokenServices tokenServices, TokenStore tokenStore) {
        this.tokenServices = tokenServices;
        this.tokenStore = tokenStore;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources
                .resourceId(resourceId)
                .tokenServices(tokenServices)
                .tokenStore(tokenStore);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .requestMatchers(EndpointRequest.to("status", "info")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                .antMatchers("/", "/**").permitAll()
                .antMatchers(
                        HttpMethod.GET,
                        "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html**",
                        "/webjars/**",
                        "favicon.ico"
                ).permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated()
                .and().httpBasic()
                .and().csrf().disable();
    }
//
//    private static class OAuthRequestedMatcher implements RequestMatcher {
//        public boolean matches(HttpServletRequest request) {
//            String auth = request.getHeader("Authorization");
//            // Determine if the client request contained an OAuth Authorization
//            boolean haveOauth2Token = (auth != null) && auth.startsWith("Bearer");
//            boolean haveAccessToken = request.getParameter("access_token")!=null;
//            return haveOauth2Token || haveAccessToken;
//        }
//    }

}