package pl.edu.pja.aurorumclinic.users.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TwoFactorAuthLoginRequest(@NotNull Long userId,
                                        @NotBlank @Size(min = 6, max = 6) String code) {
}
