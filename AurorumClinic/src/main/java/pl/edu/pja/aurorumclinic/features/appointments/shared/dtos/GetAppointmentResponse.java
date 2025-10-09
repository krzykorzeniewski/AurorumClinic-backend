package pl.edu.pja.aurorumclinic.features.appointments.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record GetAppointmentResponse(Long id,
                                     @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                     LocalDateTime startedAt,
                                     String description,
                                     DoctorDto doctor,
                                     ServiceDto service,
                                     PaymentDto payment) {
}
