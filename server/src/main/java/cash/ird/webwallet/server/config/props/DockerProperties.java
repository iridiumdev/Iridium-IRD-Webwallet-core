package cash.ird.webwallet.server.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerProperties {

    private String socket = "unix:///var/run/docker.sock";

}
