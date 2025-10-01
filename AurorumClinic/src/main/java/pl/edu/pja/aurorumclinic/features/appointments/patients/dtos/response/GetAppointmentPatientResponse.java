package pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record GetAppointmentPatientResponse(String doctorName,
                                            String doctorSurname,
                                            String doctorImage,
                                            String serviceName,
                                            @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                            LocalDateTime startedAt,
                                            @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal paymentAmount) {
}
