package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.config.props.DockerProperties;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DockerClientConfig {

    @Bean
    @Autowired
    public DockerClient dockerClient(DockerProperties dockerProperties) {
        final DockerClient docker = new DefaultDockerClient(dockerProperties.getSocket());

        dockerProperties.getPullImages().forEach(image -> {
            try {
                docker.pull(image);
            } catch (DockerException | InterruptedException e) {
                log.error("Could not pull image {}! Exception is: ", image, e);
            }
        });

        return docker;
    }

}
