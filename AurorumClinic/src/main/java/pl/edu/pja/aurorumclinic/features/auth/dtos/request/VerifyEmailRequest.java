package pl.edu.pja.aurorumclinic.features.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(@NotBlank @Size(max = 100) String token) {
}
