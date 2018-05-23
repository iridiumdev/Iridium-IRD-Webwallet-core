package cash.ird.webwallet.server.rest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(exclude = "password")
public class UserLogin {

    private String username;
    private String password;

}
