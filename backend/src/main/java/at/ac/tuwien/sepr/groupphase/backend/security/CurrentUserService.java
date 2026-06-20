package at.ac.tuwien.sepr.groupphase.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

@Component
public class CurrentUserService {

    public Long getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authentication found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof AppUserDetails user) {
            return user.getUserId();
        }

        throw new IllegalStateException("Invalid principal type: " + principal);
    }

    public boolean hasAuthority(String authority) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authentication found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof AppUserDetails user) {
            return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
        }

        throw new IllegalStateException("Invalid principal type: " + principal);
    }
}
