package pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetAppointmentPatientResponse(String doctorName,
                                            String doctorSurname,
                                            String doctorImage,
                                            String serviceName,
                                            @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                            LocalDateTime startedAt) {
}
