package pl.edu.pja.aurorumclinic.features.auth.register.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(@NotBlank @Size(max = 100) String token,
                                 @NotBlank @Email @Size(max = 100) String email) {
}
