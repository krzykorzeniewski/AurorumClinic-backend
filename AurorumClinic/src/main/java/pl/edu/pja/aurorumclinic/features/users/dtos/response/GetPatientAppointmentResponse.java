package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;

@Builder
public record GetPatientAppointmentResponse(Long id,
                                            @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                            LocalDateTime startedAt,
                                            DoctorDto doctor,
                                            ServiceDto service) {

}
