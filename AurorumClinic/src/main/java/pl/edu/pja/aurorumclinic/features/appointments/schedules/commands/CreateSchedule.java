package pl.edu.pja.aurorumclinic.features.appointments.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.schedules.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'EMPLOYEE')")
public class CreateSchedule {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createSchedule(@RequestBody @Valid CreateScheduleRequest createScheduleRequest) {
        handle(createScheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(CreateScheduleRequest request) {
        Doctor doctorFromDb = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (request.startedAt().getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (request.finishedAt().getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (request.startedAt().isAfter(request.finishedAt())) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (request.finishedAt().isBefore(request.startedAt())) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(request.startedAt(),
                request.finishedAt(), request.doctorId())) {
            throw new ApiException("Schedule overlapps with already existing one", "schedule");
        }

        Schedule schedule = Schedule.builder()
                .doctor(doctorFromDb)
                .startedAt(request.startedAt())
                .finishedAt(request.finishedAt())
                .build();
        scheduleRepository.save(schedule);
    }

    public record CreateScheduleRequest(@NotNull LocalDateTime startedAt,
                                        @NotNull LocalDateTime finishedAt,
                                        @NotNull Long doctorId) {
    }

}
