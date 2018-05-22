package cash.ird.webwallet.server.config.props;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "walletd.satellite")
public class WalletdSatelliteProperties extends SimpleContainerProperties {
}
