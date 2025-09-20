package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteAppointmentUnregisteredRequest(@NotBlank @Size(max = 100) String token) {
}
