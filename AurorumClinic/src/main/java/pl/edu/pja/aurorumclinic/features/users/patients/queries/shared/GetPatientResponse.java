package pl.edu.pja.aurorumclinic.features.users.patients.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;

import java.time.LocalDate;

@Builder
public record GetPatientResponse(Long id,
                                 String name,
                                 String surname,
                                 String pesel,
                                 LocalDate birthDate,
                                 String email,
                                 String phoneNumber,
                                 boolean twoFactorAuth,
                                 boolean newsletter,
                                 boolean emailVerified,
                                 boolean phoneNumberVerified,
                                 @JsonFormat(shape = JsonFormat.Shape.STRING) CommunicationPreference communicationPreferences) {
}

