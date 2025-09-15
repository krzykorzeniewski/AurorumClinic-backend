package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleAppointmentUnregisteredRequest(@NotNull LocalDateTime startedAt) {
}
