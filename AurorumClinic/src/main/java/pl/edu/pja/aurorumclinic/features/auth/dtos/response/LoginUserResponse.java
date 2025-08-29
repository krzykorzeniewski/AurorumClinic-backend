package pl.edu.pja.aurorumclinic.features.auth.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@Builder
public record LoginUserResponse(Long userId,

                                @JsonIgnore
                                String accessToken,

                                @JsonIgnore
                                String refreshToken,
                                boolean twoFactorAuth,
                                UserRole role) {
}
