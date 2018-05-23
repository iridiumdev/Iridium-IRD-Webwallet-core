package cash.ird.webwallet.server.config.props;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private TokenConfig accessToken;
    private TokenConfig refreshToken;


    @Data
    @NoArgsConstructor
    public static class TokenConfig{
        private int validSeconds = 600;
    }
}
