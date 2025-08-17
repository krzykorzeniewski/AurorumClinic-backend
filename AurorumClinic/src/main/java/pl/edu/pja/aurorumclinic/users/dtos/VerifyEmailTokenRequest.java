package pl.edu.pja.aurorumclinic.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
}
