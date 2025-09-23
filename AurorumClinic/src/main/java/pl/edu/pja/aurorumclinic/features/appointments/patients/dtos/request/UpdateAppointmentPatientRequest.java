package pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateAppointmentPatientRequest(@NotNull LocalDateTime startedAt,
                                              @NotNull Long appointmentId,
                                              @NotBlank String description) {
}
