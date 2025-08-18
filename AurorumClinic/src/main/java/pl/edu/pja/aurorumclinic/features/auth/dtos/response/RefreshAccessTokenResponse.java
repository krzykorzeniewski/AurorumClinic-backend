package pl.edu.pja.aurorumclinic.features.auth.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record RefreshAccessTokenResponse(Long userId,

                                         @JsonIgnore
                                         String accessToken,

                                         @JsonIgnore
                                         String refreshToken) {
}
