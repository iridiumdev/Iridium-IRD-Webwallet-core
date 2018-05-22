package cash.ird.webwallet.server.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "walletd")
public class WalletdProperties {

    private String url;

}
