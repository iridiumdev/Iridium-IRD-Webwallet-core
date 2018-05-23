package cash.ird.webwallet.server.service;

import cash.ird.webwallet.server.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private final Map<String, User> userMap = new HashMap<>();

    public UserService() {
        userMap.put("user", new User("user","$2a$10$6DwG4QBUB6eyGp.PBOGFLOIWzcYHpbtFQp4HVvqhYvxzOT3qPeGXy", Collections.singletonList(new SimpleGrantedAuthority("USER"))));
        userMap.put("admin", new User("admin","$2a$10$.FPcQaxxdrLLKZNf3ob.gewQjmlyrNtnfrJj6CFGBVyt.k2YFFPpW", Collections.singletonList(new SimpleGrantedAuthority("ADMIN"))));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userMap.getOrDefault(username, null);
    }

}
