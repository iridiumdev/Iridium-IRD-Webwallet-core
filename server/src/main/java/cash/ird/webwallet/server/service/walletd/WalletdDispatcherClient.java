package cash.ird.webwallet.server.service.walletd;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.IridiumClient;
import cash.ird.webwallet.server.config.props.WalletdProperties;
import org.springframework.stereotype.Service;

@Service
public class WalletdDispatcherClient {

    private final WalletdProperties walletdProperties;

    public WalletdDispatcherClient(WalletdProperties walletdProperties) {
        this.walletdProperties = walletdProperties;
    }


    public IridiumAPI target(String host) {
        return new IridiumClient(this.walletdProperties.getUrl(), new DispatcherHttpClient(host));
    }


}
