package pl.edu.pja.aurorumclinic.features.schedules.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.DoctorGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetAllSchedules {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DoctorGetScheduleResponse>>> getDoctorSchedules(
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt,
            @AuthenticationPrincipal Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt)));
    }

    private List<DoctorGetScheduleResponse> handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Schedule> doctorsSchedules = scheduleRepository.findAllByDoctorIdAndBetween(
                doctorId, startedAt, finishedAt);
        return doctorsSchedules.stream().map(schedule -> DoctorGetScheduleResponse.builder()
                .id(schedule.getId())
                .startedAt(schedule.getStartedAt())
                .finishedAt(schedule.getFinishedAt())
                .services(schedule.getServices().stream().map(
                        service -> DoctorGetScheduleResponse.ServiceDto.builder()
                                .id(service.getId())
                                .name(service.getName())
                                .build()
                ).toList())
                .build()).toList();
    }

}
