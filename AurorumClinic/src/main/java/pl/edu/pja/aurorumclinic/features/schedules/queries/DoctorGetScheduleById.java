package pl.edu.pja.aurorumclinic.features.schedules.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.DoctorGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetScheduleById {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/me/{id}")
    public ResponseEntity<ApiResponse<DoctorGetScheduleResponse>> docGetScheduleById(
                                                        @PathVariable("id") Long scheduleId,
                                                        @AuthenticationPrincipal Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(handle(scheduleId, doctorId)));
    }

    private DoctorGetScheduleResponse handle(Long scheduleId, Long doctorId) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        Schedule scheduleFromDb = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "scheduleId")
        );
        if (!Objects.equals(scheduleFromDb.getDoctor().getId(), doctorId)) {
            throw new ApiAuthorizationException("Schedule doctor id does not match logged in doctor id");
        }
        return DoctorGetScheduleResponse.builder()
                .id(scheduleFromDb.getId())
                .startedAt(scheduleFromDb.getStartedAt())
                .finishedAt(scheduleFromDb.getFinishedAt())
                .services(scheduleFromDb.getServices().stream().map(
                        service -> DoctorGetScheduleResponse.ServiceDto.builder()
                                .id(service.getId())
                                .name(service.getName())
                                .build()).toList())
                .build();
    }

}
