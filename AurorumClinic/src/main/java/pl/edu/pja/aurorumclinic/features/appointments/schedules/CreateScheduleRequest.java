package pl.edu.pja.aurorumclinic.features.appointments.schedules;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateScheduleRequest(@NotNull LocalDateTime startedAt,
                                    @NotNull LocalDateTime finishedAt,
                                    @NotNull Long doctorId) {
}
