package pl.edu.pja.aurorumclinic.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.edu.pja.aurorumclinic.models.enums.CommunicationPreference;

import java.time.LocalDate;

public record PutPatientRequest(@NotBlank @Size(max = 50) String name,
                                @NotBlank @Size(max = 50) String surname,
                                @NotBlank @Size(min = 11, max = 11) String pesel,
                                @NotNull LocalDate birthdate,
                                @Size(min = 9, max = 9) @NotBlank String phoneNumber,
                                @Email @NotBlank @Size(max = 100) String email,
                                @NotNull CommunicationPreference communicationPreferences,
                                boolean newsletter,
                                boolean twoFactorAuth) {
}
