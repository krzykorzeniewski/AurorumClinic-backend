package pl.edu.pja.aurorumclinic.features.auth.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record TwoFactorAuthLoginResponse(Long userId,

                                         @JsonIgnore
                                         String accessToken,

                                         @JsonIgnore
                                         String refreshToken,
                                         boolean twoFactorAuth) {
}
