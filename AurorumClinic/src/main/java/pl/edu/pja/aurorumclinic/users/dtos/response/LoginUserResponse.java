package pl.edu.pja.aurorumclinic.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record LoginUserResponse(Long userId,

                                @JsonIgnore
                                String accessToken,

                                @JsonIgnore
                                String refreshToken,
                                boolean twoFactorAuth) {
}
