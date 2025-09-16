package pl.edu.pja.aurorumclinic.features.users.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserEmailRequest(@NotBlank @Email @Size(max = 100) String email) {
}
