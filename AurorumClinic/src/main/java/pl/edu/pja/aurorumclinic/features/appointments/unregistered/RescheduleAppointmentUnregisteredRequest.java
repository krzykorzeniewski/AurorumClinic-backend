package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RescheduleAppointmentUnregisteredRequest(@NotNull LocalDateTime startedAt,
                                                       @NotBlank @Size(max = 100) String token) {
}
