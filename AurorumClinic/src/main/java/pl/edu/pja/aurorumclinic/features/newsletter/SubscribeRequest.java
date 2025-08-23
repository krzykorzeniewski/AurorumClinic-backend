package pl.edu.pja.aurorumclinic.features.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubscribeRequest(@NotBlank @Email @Size(max = 100) String email) {
}
