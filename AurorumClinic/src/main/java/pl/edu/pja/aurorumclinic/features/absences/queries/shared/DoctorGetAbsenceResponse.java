package pl.edu.pja.aurorumclinic.features.absences.queries.shared;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DoctorGetAbsenceResponse(Long id,
                                       String name,
                                       LocalDateTime startedAt,
                                       LocalDateTime finishedAt) {
}
