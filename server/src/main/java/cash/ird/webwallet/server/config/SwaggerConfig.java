package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.rest.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("cash.ird.webwallet.server.rest.controller"))
                .build()
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .ignoredParameterTypes(CurrentUser.class);
    }

}
