package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(@Size(max = 200) @NotBlank String password,
                                   @Size(max = 100) @NotBlank String token,
                                   @NotBlank @Email @Size(max = 100) String email) {
}
