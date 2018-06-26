package cash.ird.webwallet.server.config.security;

import cash.ird.webwallet.server.config.props.JwtProperties;
import cash.ird.webwallet.server.rest.model.TokenPairDto;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * A service to create JWT pair
 */
@Service
public class JWTTokenService {

    private final JwtProperties jwtProperties;
    private final JWSSigner jwsSigner;

    @Autowired
    public JWTTokenService(JwtProperties jwtProperties, JWSSigner jwsSigner) {
        this.jwtProperties = jwtProperties;
        this.jwsSigner = jwsSigner;
    }

    public TokenPairDto generateTokenPair(String subject, Collection<? extends GrantedAuthority> authorities) throws ParseException {
        SignedJWT accessToken = generateSignedToken(subject, authorities, jwtProperties, jwtProperties.getAccessToken());
        SignedJWT refreshToken = generateSignedToken(subject, authorities, jwtProperties, jwtProperties.getRefreshToken());

        return new TokenPairDto()
                .setAccessToken(accessToken.serialize())
                .setRefreshToken(refreshToken.serialize())
                .setExpiresIn(accessToken.getJWTClaimsSet().getExpirationTime().getTime());
    }

    private SignedJWT generateSignedToken(String subject, Collection<? extends GrantedAuthority> authorities, JwtProperties jwtProperties, JwtProperties.TokenProperties tokenProperties) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .expirationTime(new Date(new Date().getTime() + tokenProperties.getValidSeconds() * 1000));

        if (tokenProperties.getType() == JwtProperties.TokenType.ACCESS) {
            builder = builder.claim("authorities", authorities.parallelStream().map(auth -> (GrantedAuthority) auth).map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")));
        }

        JWTClaimsSet claimsSet = builder.build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(jwtProperties.getAlgorithm()), claimsSet);

        // HMAC protection
        try {
            signedJWT.sign(jwsSigner);
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return signedJWT;
    }
}