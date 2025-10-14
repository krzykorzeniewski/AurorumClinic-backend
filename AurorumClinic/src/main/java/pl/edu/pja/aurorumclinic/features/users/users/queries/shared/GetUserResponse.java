package pl.edu.pja.aurorumclinic.features.users.users.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
                              @JsonFormat(shape = JsonFormat.Shape.STRING) UserRole role,
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAt) {
}
