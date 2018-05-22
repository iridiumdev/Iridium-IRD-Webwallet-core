package cash.ird.webwallet.server.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerProperties {

    private String socket = "unix:///var/run/docker.sock";

    private List<String> pullImages = new ArrayList<>();

}
