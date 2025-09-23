package pl.edu.pja.aurorumclinic.features.appointments.guests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateAppointmentGuestRequest(@NotBlank @Size(max = 50) String name,
                                            @NotBlank @Size(max = 50) String surname,
                                            @Size(min = 11, max = 11) String pesel,
                                            @NotNull LocalDate birthDate,
                                            @NotBlank @Email @Size(max = 100) String email,
                                            @NotBlank @Size(max = 9) String phoneNumber,
                                            @NotNull LocalDateTime startedAt,
                                            @NotNull Long serviceId,
                                            @NotNull Long doctorId,
                                            @NotBlank String description) {
}
