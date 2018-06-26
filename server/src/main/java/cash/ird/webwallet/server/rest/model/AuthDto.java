package cash.ird.webwallet.server.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto extends UserDto{

    @JsonProperty("grant_type")
    private GrantType grantType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public enum GrantType {
        @JsonProperty("password")
        PASSWORD,

        @JsonProperty("refresh_token")
        REFRESH_TOKEN
    }

}