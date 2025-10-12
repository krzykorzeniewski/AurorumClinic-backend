package pl.edu.pja.aurorumclinic.features.users.users.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.time.LocalDate;

public record GetUserResponse(Long id,
                              String name,
                              String surname,
                              String pesel,
                              LocalDate birthDate,
                              String email,
                              String phoneNumber,
                              boolean twoFactorAuth,
                              boolean emailVerified,
                              boolean phoneNumberVerified,
                              @JsonFormat(shape = JsonFormat.Shape.STRING) UserRole role) {
}
