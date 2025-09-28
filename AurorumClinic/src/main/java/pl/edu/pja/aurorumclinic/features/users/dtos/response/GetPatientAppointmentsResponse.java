package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetPatientAppointmentsResponse(Long id,
                                             String name,
                                             String surname,
                                             String pesel,
                                             LocalDate birthdate,
                                             String email,
                                             String phoneNumber,
                                             List<AppointmentDto> appointments) {

    @Builder
    public record ServiceDto(Long id,
                             String name,
                             @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {
    }

    @Builder
    public record DoctorDto(Long id,
                            String name,
                            String surname,
                            String profilePicture) {
    }

    @Builder
    public record AppointmentDto(Long id,
                                 LocalDateTime startedAt,
                                 String description,
                                 DoctorDto doctor,
                                 ServiceDto service) {
    }
}
