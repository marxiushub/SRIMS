package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AuthTokenDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class LoginEndpoint {

    private final UserService userService;
    private final SecurityProperties securityProperties;

    public LoginEndpoint(UserService userService, SecurityProperties securityProperties) {
        this.userService = userService;
        this.securityProperties = securityProperties;
    }

    @PostMapping
    @PermitAll
    public AuthTokenDto login(@RequestBody UserLoginDto userLoginDto) {
        String token = userService.login(userLoginDto);
        AuthTokenDto out = new AuthTokenDto();
        out.setToken(token);
        out.setExpiresAt(System.currentTimeMillis() + securityProperties.getJwtExpirationTime());
        return out;
    }
}
