package pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request;

import jakarta.validation.constraints.NotNull;

public record DeleteAppointmentPatientRequest(@NotNull Long appointmentId) {
}
