package pl.edu.pja.aurorumclinic.features.auth.login.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TwoFactorAuthTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
}
