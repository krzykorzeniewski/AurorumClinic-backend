package pl.edu.pja.aurorumclinic.features.users.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUser2FATokenRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber) {
}
