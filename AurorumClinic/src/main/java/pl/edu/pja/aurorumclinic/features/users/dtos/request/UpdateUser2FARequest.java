package pl.edu.pja.aurorumclinic.features.users.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUser2FARequest(@NotBlank @Size(min = 6, max = 6) String token) {
}
