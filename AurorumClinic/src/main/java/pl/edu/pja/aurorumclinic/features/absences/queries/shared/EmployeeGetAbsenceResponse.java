package pl.edu.pja.aurorumclinic.features.absences.queries.shared;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EmployeeGetAbsenceResponse(Long id,
                                         String name,
                                         LocalDateTime startedAt,
                                         LocalDateTime finishedAt,
                                         DoctorDto doctor) {
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
