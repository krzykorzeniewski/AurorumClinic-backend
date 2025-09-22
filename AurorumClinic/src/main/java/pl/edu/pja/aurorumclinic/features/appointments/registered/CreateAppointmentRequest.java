package pl.edu.pja.aurorumclinic.features.appointments.registered;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAppointmentRequest(Long patientId,
                                       @NotNull LocalDateTime startedAt,
                                       @NotNull Long serviceId,
                                       @NotNull Long doctorId,
                                       @NotBlank String description) {
}
