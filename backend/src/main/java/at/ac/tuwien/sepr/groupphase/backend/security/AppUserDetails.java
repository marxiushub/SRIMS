package at.ac.tuwien.sepr.groupphase.backend.security;

import org.springframework.security.core.userdetails.User;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public class AppUserDetails extends User {
    private final Long userId;

    public AppUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long userId) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
