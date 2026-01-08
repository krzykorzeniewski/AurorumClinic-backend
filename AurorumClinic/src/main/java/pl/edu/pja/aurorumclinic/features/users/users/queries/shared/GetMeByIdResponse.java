package pl.edu.pja.aurorumclinic.features.users.users.queries.shared;

import java.time.LocalDate;

public record GetMeByIdResponse(Long id,
                                String name,
                                String surname,
                                String pesel,
                                LocalDate birthDate,
                                String email,
                                String phoneNumber,
                                boolean twoFactorAuth,
                                boolean emailVerified,
                                boolean phoneNumberVerified) {
}
