package cash.ird.webwallet.server.config.props;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public abstract class SimpleContainerProperties {

    private String name;
    private String image;
    private List<String> command;
    private Set<String> ports;
    private Set<String> volumes;

}
