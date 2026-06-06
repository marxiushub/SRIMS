package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;

import java.util.HashSet;
import java.util.Set;

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
