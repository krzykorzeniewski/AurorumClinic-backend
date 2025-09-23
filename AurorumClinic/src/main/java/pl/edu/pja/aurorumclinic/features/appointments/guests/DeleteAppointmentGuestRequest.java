package pl.edu.pja.aurorumclinic.features.appointments.guests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteAppointmentGuestRequest(@NotBlank @Size(max = 100) String token) {
}
