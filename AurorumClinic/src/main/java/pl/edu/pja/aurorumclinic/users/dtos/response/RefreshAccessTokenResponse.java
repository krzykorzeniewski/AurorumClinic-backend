package pl.edu.pja.aurorumclinic.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record RefreshAccessTokenResponse(Long userId,

                                         @JsonIgnore
                                         String accessToken,

                                         @JsonIgnore
                                         String refreshToken) {
}
