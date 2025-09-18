package pl.edu.pja.aurorumclinic.features.users.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserEmailRequest(@Size(max = 100) @NotBlank String token) {
}
