package cash.ird.webwallet.server;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class BcryptTest {

    @Ignore
    @Test
    public void encryptPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String enc = encoder.encode("secret");
        log.info(enc);

        assert enc != null;
    }
}
