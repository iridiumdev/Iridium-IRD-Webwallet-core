package cash.ird.webwallet.server.service;

import cash.ird.webwallet.server.config.props.SimpleContainerProperties;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.*;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

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

        builder.cmd(containerProperties.getCommand());

        if (containerProperties.getPorts() != null || containerProperties.getVolumes() != null) {
            HostConfig.Builder hostBuilder = HostConfig.builder();

            containerProperties.getPorts().forEach(portMapping -> {
                String[] ports = portMapping.split(":");
                hostBuilder.portBindings(
                        ImmutableMap.of(ports[1], Collections.singletonList(PortBinding.of("", ports[0])))
                );
                builder.exposedPorts(ports[1]);
            });

            containerProperties.getVolumes().forEach(hostBuilder::appendBinds);

            builder.hostConfig(hostBuilder.build());

        }

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

    public ContainerInfo inspectContainer(String id) throws DockerException, InterruptedException {
        return dockerClient.inspectContainer(id);
    }

    public ContainerInfo findContainer(String id) throws DockerException, InterruptedException {
        try {
            return dockerClient.inspectContainer(id);
        } catch (ContainerNotFoundException e) {
            return null;
        }
    }



    @SuppressWarnings("UnusedReturnValue")
    public boolean removeContainerIfExisting(String id) throws DockerException, InterruptedException {
        try {
            dockerClient.stopContainer(id, 0);
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

    public Volume createVolume(String volumeName) throws DockerException, InterruptedException {
        final Volume toCreate = Volume.builder()
                .name(volumeName)
                .driver("local")
                .build();
        return dockerClient.createVolume(toCreate);
    }

    public void renameVolume(String oldVolumeName, String newVolumeName) throws DockerException, InterruptedException {
        final Volume newVolume = Volume.builder()
                .name(newVolumeName)
                .driver("local")
                .build();
        dockerClient.createVolume(newVolume);

        ContainerConfig.Builder builder = ContainerConfig
                .builder()
                .image("alpine")
                .cmd("ash", "-c", "cp -av /from/* /to")
                .hostConfig(
                        HostConfig.builder()
                                .appendBinds(String.format("%s:%s", oldVolumeName, "/from"))
                                .appendBinds(String.format("%s:%s", newVolumeName, "/to"))
                                .build()
                );

        ContainerCreation containerCreation = createContainerFromBuilder(builder);

        dockerClient.startContainer(containerCreation.id());

        final ContainerExit exit = dockerClient.waitContainer(containerCreation.id());


        //noinspection ConstantConditions
        if (exit.statusCode() == 0) {
            removeContainerIfExisting(containerCreation.id());
            dockerClient.removeVolume(oldVolumeName);
        } else {
            removeContainerIfExisting(containerCreation.id());
            throw new DockerException(String.format("Could not rename volume from %s to %s", oldVolumeName, newVolumeName));
        }


    }

}

