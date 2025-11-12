package pl.edu.pja.aurorumclinic.features.schedules.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.EmployeeGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAllSchedules {

    private final ScheduleRepository scheduleRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<EmployeeGetScheduleResponse>>> getAllSchedules(@PageableDefault Pageable pageable,
                                                                                          LocalDateTime startedAt,
                                                                                          LocalDateTime finishedAt) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt, pageable)));
    }

    private Page<EmployeeGetScheduleResponse> handle(LocalDateTime startedAt, LocalDateTime finishedAt, Pageable pageable) {
        Page<Schedule> schedulesFromDb = scheduleRepository.findAllSchedulesBetweenDates(startedAt, finishedAt, pageable);
        return schedulesFromDb.map(schedule -> EmployeeGetScheduleResponse.builder()
                .id(schedule.getId())
                .startedAt(schedule.getStartedAt())
                .finishedAt(schedule.getFinishedAt())
                .doctor(EmployeeGetScheduleResponse.DoctorDto.builder()
                        .id(schedule.getDoctor().getId())
                        .name(schedule.getDoctor().getName())
                        .surname(schedule.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(schedule.getDoctor().getProfilePicture()))
                        .specializations(schedule.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetScheduleResponse.DoctorDto.SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .services(schedule.getServices().stream().map(service -> EmployeeGetScheduleResponse.ServiceDto.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .build()).toList())
                .build());
    }

}
