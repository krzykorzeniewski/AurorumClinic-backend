package pl.edu.pja.aurorumclinic.features.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TwoFactorAuthLoginRequest(@NotBlank @Size(min = 6, max = 6) String token,
                                        @NotBlank Long userId) {
}
