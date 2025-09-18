package pl.edu.pja.aurorumclinic.features.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPhoneNumberRequest(@NotBlank @Size(max = 6, min = 6) String token,
                                       @NotBlank @Size(max = 9, min = 9) String phoneNumber) {
}
