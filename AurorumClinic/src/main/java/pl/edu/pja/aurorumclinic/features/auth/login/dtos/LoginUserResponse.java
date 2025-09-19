package pl.edu.pja.aurorumclinic.features.auth.login.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@Builder
public record LoginUserResponse(Long userId,
                                String email,
                                boolean twoFactorAuth,
                                UserRole role,
                                @JsonIgnore String accessToken,
                                @JsonIgnore String refreshToken) {
}
