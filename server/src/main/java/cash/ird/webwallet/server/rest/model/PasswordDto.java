package cash.ird.webwallet.server.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class PasswordDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonView(Shadow.class)
    private String password;

    interface Shadow{}

}
