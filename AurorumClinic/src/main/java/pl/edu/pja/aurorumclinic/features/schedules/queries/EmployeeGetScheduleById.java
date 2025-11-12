package pl.edu.pja.aurorumclinic.features.schedules.queries;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.EmployeeGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetScheduleById {

    private final ScheduleRepository scheduleRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeGetScheduleResponse>> empGetScheduleById(
            @PathVariable("id") Long scheduleId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(scheduleId)));
    }

    private EmployeeGetScheduleResponse handle(Long scheduleId) {
        Schedule scheduleFromDb = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "scheduleId")
        );
        return EmployeeGetScheduleResponse.builder()
                .id(scheduleFromDb.getId())
                .startedAt(scheduleFromDb.getStartedAt())
                .finishedAt(scheduleFromDb.getFinishedAt())
                .doctor(EmployeeGetScheduleResponse.DoctorDto.builder()
                        .id(scheduleFromDb.getDoctor().getId())
                        .name(scheduleFromDb.getDoctor().getName())
                        .surname(scheduleFromDb.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(scheduleFromDb.getDoctor().getProfilePicture()))
                        .specializations(scheduleFromDb.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetScheduleResponse.DoctorDto.SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .services(scheduleFromDb.getServices().stream().map(service -> EmployeeGetScheduleResponse.ServiceDto.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .build()).toList())
                .build();
    }

}
