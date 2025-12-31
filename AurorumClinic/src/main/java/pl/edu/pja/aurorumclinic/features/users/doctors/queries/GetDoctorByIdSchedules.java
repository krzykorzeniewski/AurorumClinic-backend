package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class GetDoctorByIdSchedules {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/{id}/schedules")
    public ResponseEntity<ApiResponse<List<GetDoctorSchedulesResponse>>> getDoctorSchedules(
            @PathVariable("id") Long doctorId,
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt) {
            return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt)));
    }

    private List<GetDoctorSchedulesResponse> handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Schedule> doctorsSchedules = scheduleRepository.findAllByDoctorIdAndBetween(
                doctorId, startedAt, finishedAt);
        return doctorsSchedules.stream().map(schedule -> GetDoctorSchedulesResponse.builder()
                .id(schedule.getId())
                .startedAt(schedule.getStartedAt())
                .finishedAt(schedule.getFinishedAt())
                .services(schedule.getServices().stream().map(
                        service -> GetDoctorSchedulesResponse.ServiceDto.builder()
                                .id(service.getId())
                                .name(service.getName())
                                .build()
                ).toList())
                .build()).toList();
    }

    @Builder
    record GetDoctorSchedulesResponse(Long id,
                                      LocalDateTime startedAt,
                                      LocalDateTime finishedAt,
                                      List<ServiceDto> services
                                      ) {
        @Builder
        record ServiceDto(Long id,
                          String name) {

        }
    }

}
