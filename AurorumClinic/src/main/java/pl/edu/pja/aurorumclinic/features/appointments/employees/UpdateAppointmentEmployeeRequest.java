package pl.edu.pja.aurorumclinic.features.appointments.employees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateAppointmentEmployeeRequest(@NotNull Long patientId,
                                               @NotNull LocalDateTime startedAt,
                                               @NotBlank String description) {
}
