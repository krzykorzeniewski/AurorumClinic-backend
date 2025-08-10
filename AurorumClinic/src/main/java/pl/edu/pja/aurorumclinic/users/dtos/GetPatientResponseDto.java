package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetPatientResponseDto(Long id,
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
