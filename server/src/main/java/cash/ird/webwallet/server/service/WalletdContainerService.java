package cash.ird.webwallet.server.service;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.model.request.PrivateKey;
import cash.ird.webwallet.server.config.WalletdConfig;
import cash.ird.webwallet.server.config.props.WalletdSatelliteProperties;
import cash.ird.webwallet.server.service.model.WalletContainerInstance;
import cash.ird.webwallet.server.service.walletd.WalletdDispatcherClient;
import com.spotify.docker.client.messages.*;
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

    public Mono<String> createWallet(String viewSecretKey, String spendSecretKey, String password) {

        return Mono.create(sink -> {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String containerSetupName = String.format("%s.setup", uuid);


            try {

                List<String> cmd = new ArrayList<>(satelliteProperties.getCommand());
                cmd.add(String.format("--container-password=%s", password));

                ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(satelliteProperties)
                        .cmd(cmd)
                        .env(
                                String.format("VIRTUAL_HOST=%s", containerSetupName),
                                "VIRTUAL_PORT=14007"
                        );



                Volume setupVolume = dockerService.createVolume(containerSetupName);

                builder.hostConfig(
                        HostConfig.builder()
                        .appendBinds(String.format("%s:%s", setupVolume.name(), "/data"))
                        .build()
                );


                ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);
                dockerService.connectToNetwork(containerCreation.id(), walletdNetwork.getId());
                dockerService.renameContainer(containerCreation.id(), containerSetupName);
                dockerService.startContainer(containerCreation.id());

                Thread.sleep(5000); // TODO: 22.05.18 - nah.

                IridiumAPI iridiumAPI = walletdDispatcherClient.target(containerSetupName);

                iridiumAPI.reset(viewSecretKey);
                String address = iridiumAPI.createAddress(PrivateKey.of(spendSecretKey));

                dockerService.removeContainerIfExisting(containerSetupName);
                dockerService.renameVolume(containerSetupName, String.format("%s.wallet", address));

                sink.success(address);

            } catch (Exception e) {
                sink.error(e);
            }

        });

    }

    public Mono<WalletContainerInstance> loadWallet(String address, String password) {
        return Mono.create(sink -> {

            try {
                String containerName = String.format("%s.wallet", address);

                ContainerInfo containerInfo = dockerService.findContainer(containerName);
                if (containerInfo != null && containerInfo.state().running()) {
                    sink.success(WalletContainerInstance.of(address, containerName));
                } else {
                    List<String> cmd = new ArrayList<>(satelliteProperties.getCommand());
                    cmd.add(String.format("--container-password=%s", password));

                    ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(satelliteProperties)
                            .cmd(cmd)
                            .env(
                                    String.format("VIRTUAL_HOST=%s", containerName),
                                    "VIRTUAL_PORT=14007"
                            );

                    builder.hostConfig(
                            HostConfig.builder()
                                    .appendBinds(String.format("%s:%s", containerName, "/data"))
                                    .build()
                    );

                    ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);
                    dockerService.connectToNetwork(containerCreation.id(), walletdNetwork.getId());
                    dockerService.renameContainer(containerCreation.id(), containerName);
                    dockerService.startContainer(containerCreation.id());
                    Thread.sleep(5000); // TODO: 22.05.18 - nah.

                    sink.success(WalletContainerInstance.of(address, containerName));
                }

            } catch (Exception e) {
                sink.error(e);
            }

        });
    }


}
