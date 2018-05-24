package cash.ird.webwallet.server.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(exclude = "password")
public class UserCredentialsDto extends UserDto{

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonView(Shadow.class)
    private String password;

    interface Shadow{}
}
