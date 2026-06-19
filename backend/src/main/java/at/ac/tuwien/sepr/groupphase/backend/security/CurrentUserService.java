package at.ac.tuwien.sepr.groupphase.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
}
