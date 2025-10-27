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
public class GetAllSchedules {

    private final ScheduleRepository scheduleRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetScheduleResponse>>> getAllSchedules(@PageableDefault Pageable pageable,
                                                                                  LocalDateTime startedAt,
                                                                                  LocalDateTime finishedAt) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt, pageable)));
    }

    private Page<GetScheduleResponse> handle(LocalDateTime startedAt, LocalDateTime finishedAt, Pageable pageable) {
        Page<Schedule> schedulesFromDb = scheduleRepository.findAllSchedulesBetweenDates(startedAt, finishedAt, pageable);
        return schedulesFromDb.map(schedule -> GetScheduleResponse.builder()
                .id(schedule.getId())
                .startedAt(schedule.getStartedAt())
                .finishedAt(schedule.getFinishedAt())
                .doctor(GetScheduleResponse.DoctorDto.builder()
                        .id(schedule.getDoctor().getId())
                        .name(schedule.getDoctor().getName())
                        .surname(schedule.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(schedule.getDoctor().getProfilePicture()))
                        .specializations(schedule.getDoctor().getSpecializations().stream()
                                .map(specialization -> GetScheduleResponse.DoctorDto.SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .services(schedule.getServices().stream().map(service -> GetScheduleResponse.ServiceDto.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .build()).toList())
                .build());
    }

    @Builder
    record GetScheduleResponse(Long id,
                               LocalDateTime startedAt,
                               LocalDateTime finishedAt,
                               DoctorDto doctor,
                               List<ServiceDto> services) {
        @Builder
        record DoctorDto(Long id,
                         String name,
                         String surname,
                         String profilePicture,
                         List<SpecializationDto> specializations) {

            @Builder
            record SpecializationDto(Long id,
                                     String name) {

            }
        }
        @Builder
        record ServiceDto(Long id,
                          String name) {
        }
    }

}
