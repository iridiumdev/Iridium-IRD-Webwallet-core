package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.rest.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .ignoredParameterTypes(CurrentUser.class);
    }

}
