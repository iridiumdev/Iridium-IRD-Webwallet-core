package cash.ird.webwallet.server.service;

import cash.ird.webwallet.server.config.props.SimpleContainerProperties;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.*;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class DockerService {

    private final DockerClient dockerClient;

    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public ContainerConfig.Builder buildConfigFromProperties(SimpleContainerProperties containerProperties) {
        ContainerConfig.Builder builder = ContainerConfig
                .builder();

        builder.image(containerProperties.getImage());

//        if (containerProperties.getPorts() != null) {
//
//        }

//        if (containerProperties.getVolumes() != null) {
//            builder.volumes(containerProperties.getVolumes());
//        }

        return builder;
    }

    public ContainerCreation createContainerFromBuilder(ContainerConfig.Builder builder) throws DockerException, InterruptedException {
        ContainerConfig config = builder.build();

        try {
            return dockerClient.createContainer(config);
        } catch (DockerException | InterruptedException e) {
            log.error("Could not create container from builder={}", builder);
            throw e;
        }
    }

    public void startContainer(String id) throws DockerException, InterruptedException {
        dockerClient.startContainer(id);
    }

    public void renameContainer(String id, String name) throws DockerException, InterruptedException {
        dockerClient.renameContainer(id, name);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeContainerIfExisting(String id) throws DockerException, InterruptedException {
        try {
            dockerClient.removeContainer(id);
        } catch (ContainerNotFoundException e) {
            log.info("Container {} not found. Nothing to delete.", id);
            log.debug("Could not find container {}", id, e);
            return false;
        }
        return true;
    }

    public String createNetwork(String name) throws DockerException, InterruptedException {

        try {
            Network net = dockerClient.inspectNetwork(name);
            log.info("Network {} already created nothing to do.", name);
            return net.id();
        } catch (NetworkNotFoundException e) {
            log.info("Network {} not created yet, going to create it now.", name);

            NetworkConfig networkConfig = NetworkConfig.builder()
                    .attachable(true)
                    .checkDuplicate(true)
                    .name(name)
                    .build();
            return dockerClient.createNetwork(networkConfig).id();
        }



    }

    public void connectToNetwork(String containerId, String networkId) throws DockerException, InterruptedException {
        dockerClient.connectToNetwork(containerId, networkId);
    }

    public void disconnectFromNetwork(String containerId, String networkId) throws DockerException, InterruptedException {
        dockerClient.disconnectFromNetwork(containerId, networkId);
    }

}

