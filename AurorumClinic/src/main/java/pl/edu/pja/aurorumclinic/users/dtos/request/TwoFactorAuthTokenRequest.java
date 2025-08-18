package pl.edu.pja.aurorumclinic.users.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TwoFactorAuthTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
}
