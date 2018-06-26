package cash.ird.webwallet.server.config.props;

import com.nimbusds.jose.JWSAlgorithm;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("jwt")
public class JwtProperties {

    private String issuer;

    private String secret;

    private JWSAlgorithm algorithm;

    private TokenProperties accessToken;

    private TokenProperties refreshToken;

    @Getter @Setter
    public static class TokenProperties {
        private Long validSeconds = 60L;
        private TokenType type;
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }

}
