package pl.edu.pja.aurorumclinic.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatchPatientRequestDto(@Size(min = 9, max = 9) @NotBlank String phoneNumber,
                                     @Email @NotBlank @Size(max = 100) String email,
                                     @Size (max = 50) @NotBlank String communicationPreferences,
                                     boolean newsletter,
                                     boolean twoFactorAuth) {
}
