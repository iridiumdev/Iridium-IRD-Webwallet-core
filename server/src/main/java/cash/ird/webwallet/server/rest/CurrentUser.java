package cash.ird.webwallet.server.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import springfox.documentation.annotations.ApiIgnore;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiIgnore
@AuthenticationPrincipal(expression = "@userRepository.findByUsername(#this).orElse(null)")
public @interface CurrentUser {
}