package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PermissionService {

    public Set<String> getEffectivePermissions(ApplicationUser user) {

        Set<String> perms = new HashSet<>();

        user.getRoles().forEach(role ->
            role.getPermissions().forEach(p -> perms.add(p.getName()))
        );

        user.getDirectPermissions().forEach(p ->
            perms.add(p.getName())
        );

        return perms;
    }
}
