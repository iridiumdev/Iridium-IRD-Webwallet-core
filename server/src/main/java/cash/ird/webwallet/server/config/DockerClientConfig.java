package cash.ird.webwallet.server.config;

import cash.ird.webwallet.server.config.props.DockerProperties;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfig {

    @Bean
    @Autowired
    public DockerClient dockerClient(DockerProperties dockerProperties) {
        final DockerClient docker = new DefaultDockerClient(dockerProperties.getSocket());
        return docker;
    }

}
