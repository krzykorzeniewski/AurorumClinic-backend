package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppointmentDto(Long id,
                      LocalDateTime startedAt,
                      String description,
                      DoctorDto doctor,
                      ServiceDto service) {

}
