package cash.ird.webwallet.server.service;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.model.request.PrivateKey;
import cash.ird.webwallet.server.config.WalletdConfig;
import cash.ird.webwallet.server.config.props.WalletdSatelliteProperties;
import cash.ird.webwallet.server.service.model.WalletContainerMapping;
import cash.ird.webwallet.server.service.walletd.WalletdDispatcherClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WalletdContainerService {

    private final DockerService dockerService;
    private final WalletdSatelliteProperties satelliteProperties;
    private final WalletdConfig.WalletdNetwork walletdNetwork;
    private final WalletdDispatcherClient walletdDispatcherClient;

    @Autowired
    public WalletdContainerService(DockerService dockerService, WalletdSatelliteProperties satelliteProperties, WalletdConfig.WalletdNetwork walletdNetwork, WalletdDispatcherClient walletdDispatcherClient) {
        this.dockerService = dockerService;
        this.satelliteProperties = satelliteProperties;
        this.walletdNetwork = walletdNetwork;
        this.walletdDispatcherClient = walletdDispatcherClient;
    }

    // TODO: 22.05.18 - make this actually reactive *facepalm*
    public Mono<WalletContainerMapping> createWallet(String viewSecretKey, String spendSecretKey, String password) {

        return Mono.create(sink -> {
            String uuid = UUID.randomUUID().toString();

            List<String> cmd = new ArrayList<>(satelliteProperties.getCommand());
            cmd.add(String.format("--container-password=%s", password));

            ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(satelliteProperties)
                    .cmd(cmd)
                    .env(
                            String.format("VIRTUAL_HOST=%s.wallet", uuid),
                            "VIRTUAL_PORT=14007"
                    );


            try {

                ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);
                dockerService.connectToNetwork(containerCreation.id(), walletdNetwork.getId());
                dockerService.renameContainer(containerCreation.id(), uuid);
                dockerService.startContainer(containerCreation.id());

                Thread.sleep(5000); // TODO: 22.05.18 - nah.

                IridiumAPI iridiumAPI = walletdDispatcherClient.target(String.format("%s.wallet", uuid));

                iridiumAPI.reset(viewSecretKey);
                String address = iridiumAPI.createAddress(PrivateKey.of(spendSecretKey));

                sink.success(WalletContainerMapping.of(address, uuid));

            } catch (Exception e) {
                sink.error(e);
            }

        });

    }


}
