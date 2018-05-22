package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.config.props.WalletdDispatcherProperties;
import cash.ird.webwallet.server.config.props.WalletdNetworkProperties;
import cash.ird.webwallet.server.service.DockerService;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class WalletdConfig {

    @Bean
    @Autowired
    public WalletdDispatcherContainer walletdDispatcher(DockerService dockerService, WalletdDispatcherProperties dispatcherProperties, WalletdNetwork walletdNetwork) throws DockerException, InterruptedException {
        dockerService.removeContainerIfExisting(dispatcherProperties.getName());

        ContainerConfig.Builder builder = dockerService.buildConfigFromProperties(dispatcherProperties);
        HostConfig hostConfig = HostConfig.builder()
                .appendBinds("/var/run/docker.sock:/tmp/docker.sock:ro")
                .portBindings( ImmutableMap.of( "80/tcp", Arrays.asList( PortBinding.of( "", "14999" ) ) ) )
                .build();

        builder.exposedPorts("80/tcp");

        builder.hostConfig(hostConfig);

        ContainerCreation containerCreation = dockerService.createContainerFromBuilder(builder);

        WalletdDispatcherContainer dispatcherContainer = WalletdDispatcherContainer.of(containerCreation.id());

        dockerService.connectToNetwork(dispatcherContainer.getId(), walletdNetwork.getId());

        dockerService.renameContainer(dispatcherContainer.getId(), dispatcherProperties.getName());

        dockerService.startContainer(dispatcherContainer.getId());

        return dispatcherContainer;
    }

    @Bean
    @Autowired
    public WalletdNetwork walletdNetwork(DockerService dockerService, WalletdNetworkProperties networkProperties) throws DockerException, InterruptedException {
        return WalletdNetwork.of(dockerService.createNetwork(networkProperties.getName()));
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    public static class WalletdDispatcherContainer {
        private final String id;
    }

    @Data
    @RequiredArgsConstructor(staticName = "of")
    public static class WalletdNetwork {
        private final String id;
    }

}
