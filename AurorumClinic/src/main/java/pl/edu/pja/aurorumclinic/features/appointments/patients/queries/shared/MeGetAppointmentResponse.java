package pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MeGetAppointmentResponse(Long id,
                                       @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                       LocalDateTime startedAt,
                                       String description,
                                       DoctorDto doctor,
                                       ServiceDto service,
                                       PaymentDto payment) {
    @Builder
    public record PaymentDto(Long id,
                             @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal amount,
                             @JsonFormat(shape = JsonFormat.Shape.STRING) PaymentStatus status) {
    }
    @Builder
    public record ServiceDto(Long id,
                             String name,
                             @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {
    }
    @Builder
    public record DoctorDto(Long id,
                     String name,
                     String surname,
                     String profilePicture,
                     List<SpecializationDto> specializations) {
        @Builder
        public record SpecializationDto(Long id,
                                 String name) {

        }
    }

}
