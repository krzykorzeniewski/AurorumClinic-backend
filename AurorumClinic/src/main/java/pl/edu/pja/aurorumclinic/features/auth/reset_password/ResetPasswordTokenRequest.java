package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
}
