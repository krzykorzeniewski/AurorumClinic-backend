package pl.edu.pja.aurorumclinic.features.auth.login.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefreshAccessTokenRequest(@NotNull @Size(max = 1000) String accessToken,
                                        @NotNull @Size(max = 200) String refreshToken) {
}
