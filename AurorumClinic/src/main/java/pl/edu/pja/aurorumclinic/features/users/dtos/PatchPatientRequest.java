package pl.edu.pja.aurorumclinic.features.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;

public record PatchPatientRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber,
                                  @Email @NotBlank @Size(max = 100) String email,
                                  @NotNull CommunicationPreference communicationPreferences,
                                  boolean newsletter,
                                  boolean twoFactorAuth) {
}
