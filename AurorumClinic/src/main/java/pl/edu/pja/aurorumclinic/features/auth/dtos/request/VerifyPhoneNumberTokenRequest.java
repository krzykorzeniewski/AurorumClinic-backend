package pl.edu.pja.aurorumclinic.features.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPhoneNumberTokenRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber) {
}
