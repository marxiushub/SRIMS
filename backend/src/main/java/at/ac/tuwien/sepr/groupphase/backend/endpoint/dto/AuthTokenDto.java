package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class AuthTokenDto {

    private String token;
    private Long expiresAt;

    public AuthTokenDto() {}

    public AuthTokenDto(String token, Long expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    //Getter und Setter

    public String getToken() {
        return token;
    }

    public  void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
