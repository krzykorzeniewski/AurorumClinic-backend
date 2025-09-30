package pl.edu.pja.aurorumclinic.features.auth.login.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.lang.Nullable;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@Builder
public record LoginUserResponse(boolean twoFactorAuth,
                                UserRole role,
                                @JsonIgnore String accessToken,
                                @JsonIgnore String refreshToken) {
}
