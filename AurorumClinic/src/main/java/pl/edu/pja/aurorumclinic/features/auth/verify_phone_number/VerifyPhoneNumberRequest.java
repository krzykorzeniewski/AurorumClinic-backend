package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPhoneNumberRequest(@NotBlank @Size(max = 6, min = 6) String token) {
}
