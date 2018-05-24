package cash.ird.webwallet.server.service;

import cash.ird.walletd.IridiumAPI;
import cash.ird.walletd.model.request.PrivateKey;
import cash.ird.webwallet.server.config.WalletdConfig;
import cash.ird.webwallet.server.config.props.WalletdSatelliteProperties;
import cash.ird.webwallet.server.domain.Wallet;
import cash.ird.webwallet.server.exception.WalletNotLoadedException;
import cash.ird.webwallet.server.rest.model.WalletContainerDto;
import cash.ird.webwallet.server.service.model.WalletContainerInstance;
import cash.ird.webwallet.server.service.walletd.WalletdDispatcherClient;
import com.spotify.docker.client.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Mono<WalletContainerInstance> createWalletContainer(String containerHash, String viewSecretKey, String spendSecretKey, String password) {

        return Mono.create(sink -> {
            String setupContainerName = String.format("%s.setup", containerHash);
            String walletContainerName = String.format("%s.wallet", containerHash);


            try {

                List<String> cmd = new ArrayList<>(satelliteProperties.getCommand());
                cmd.add(String.format("--container-password=%s", password));

                ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(satelliteProperties)
                        .cmd(cmd)
                        .env(
                                String.format("VIRTUAL_HOST=%s", setupContainerName),
                                "VIRTUAL_PORT=14007"
                        );


                Volume volume = dockerService.createVolume(walletContainerName);

                builder.hostConfig(
                        HostConfig.builder()
                        .appendBinds(String.format("%s:%s", volume.name(), "/data"))
                        .build()
                );


                ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);
                dockerService.connectToNetwork(containerCreation.id(), walletdNetwork.getId());
                dockerService.renameContainer(containerCreation.id(), setupContainerName);
                dockerService.startContainer(containerCreation.id());

                Thread.sleep(5000); // TODO: 22.05.18 - nah.

                IridiumAPI iridiumAPI = walletdDispatcherClient.target(setupContainerName);

                iridiumAPI.reset(viewSecretKey);
                String address = iridiumAPI.createAddress(PrivateKey.of(spendSecretKey));

                dockerService.removeContainerIfExisting(setupContainerName);

                sink.success(WalletContainerInstance.of(address, containerHash));

            } catch (Exception e) {
                sink.error(e);
            }

        });

    }

    private Mono<Wallet> probeWalletLoaded(Wallet wallet) {
        return getWalletContainerStatus(wallet)
                .flatMap(walletContainerDto -> Mono.create(sink -> {
                    if (walletContainerDto.getContainerStatus() == WalletContainerDto.ContainerStatus.RUNNING)
                        sink.success(wallet);
                    else
                        sink.success();
                }));
    }

    public Mono<Wallet> checkWalletLoaded(Wallet wallet) {

        return probeWalletLoaded(wallet)
                .switchIfEmpty(Mono.error(new WalletNotLoadedException("Wallet not loaded!")));

    }

    public Mono<WalletContainerDto> checkWalletContainerLoaded(Wallet wallet) {

        return getWalletContainerStatus(wallet)
                .flatMap(walletContainerDto -> Mono.create(sink -> {
                    if (walletContainerDto.getContainerStatus() == WalletContainerDto.ContainerStatus.RUNNING)
                        sink.success(walletContainerDto);
                    else
                        sink.error(new WalletNotLoadedException("Wallet not loaded!"));
                }));

    }


    public Mono<WalletContainerDto> getWalletContainerStatus(Wallet wallet){
        return Mono.create(sink -> {

            WalletContainerDto walletContainerDto = new WalletContainerDto();
            walletContainerDto.setAddress(wallet.getAddress());
            walletContainerDto.setContainerStatus(WalletContainerDto.ContainerStatus.DELETED);
            walletContainerDto.setContainerName(wallet.getContainerName());

            try {
                Optional<ContainerInfo> infoOptional = dockerService.findContainer(wallet.getContainerName());
                if (infoOptional.isPresent() && infoOptional.get().state().running()) {
                    walletContainerDto.setContainerStatus(WalletContainerDto.ContainerStatus.RUNNING);
                } else {
                    walletContainerDto.setContainerStatus(WalletContainerDto.ContainerStatus.DELETED);
                }
            } catch (Exception e) {
                log.error("Error during status query of container {}. Exception is: ", wallet.getContainerName(), e);
                walletContainerDto.setContainerStatus(WalletContainerDto.ContainerStatus.ERROR);
            }
            sink.success(walletContainerDto);
        });
    }

    public Mono<WalletContainerDto> loadWalletContainer(Wallet wallet, String password) {

        return probeWalletLoaded(wallet)
                .switchIfEmpty(Mono.create(sink -> {
                    try {


                        List<String> cmd = new ArrayList<>(satelliteProperties.getCommand());
                        cmd.add(String.format("--container-password=%s", password));

                        ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(satelliteProperties)
                                .cmd(cmd)
                                .env(
                                        String.format("VIRTUAL_HOST=%s", wallet.getContainerName()),
                                        "VIRTUAL_PORT=14007"
                                );

                        builder.hostConfig(
                                HostConfig.builder()
                                        .appendBinds(String.format("%s:%s", wallet.getContainerName(), "/data"))
                                        .build()
                        );

                        ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);
                        dockerService.connectToNetwork(containerCreation.id(), walletdNetwork.getId());
                        dockerService.renameContainer(containerCreation.id(), wallet.getContainerName());
                        dockerService.startContainer(containerCreation.id());
                        Thread.sleep(5000); // TODO: 22.05.18 - nah.

                        sink.success(wallet);


                    } catch (Exception e) {
                        sink.error(e);
                    }

                }))
                .flatMap(this::checkWalletContainerLoaded)
                .retry(3);

    }


}
