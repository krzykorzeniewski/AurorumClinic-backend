package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import lombok.Builder;

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
                                 String communicationPreferences) {
}
