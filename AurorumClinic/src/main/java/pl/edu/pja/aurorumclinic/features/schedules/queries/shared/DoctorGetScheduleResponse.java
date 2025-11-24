package pl.edu.pja.aurorumclinic.features.schedules.queries.shared;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DoctorGetScheduleResponse(Long id,
                                        LocalDateTime startedAt,
                                        LocalDateTime finishedAt,
                                        List<ServiceDto> services) {
    @Builder
    public record ServiceDto(Long id,
                      String name) {
    }
}
