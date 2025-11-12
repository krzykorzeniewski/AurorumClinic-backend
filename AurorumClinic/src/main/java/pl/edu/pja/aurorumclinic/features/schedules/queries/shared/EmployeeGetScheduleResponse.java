package pl.edu.pja.aurorumclinic.features.schedules.queries.shared;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.features.schedules.queries.EmployeeGetAllSchedules;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EmployeeGetScheduleResponse(Long id,
                                          LocalDateTime startedAt,
                                          LocalDateTime finishedAt,
                                          DoctorDto doctor,
                                          List<ServiceDto> services) {
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
    @Builder
    public record ServiceDto(Long id,
                      String name) {
    }
}
