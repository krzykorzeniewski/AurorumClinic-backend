package pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record PatientGetAppointmentResponse(Long id,
                                            @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                            LocalDateTime startedAt,
                                            DoctorDto doctor,
                                            ServiceDto service) {

}
