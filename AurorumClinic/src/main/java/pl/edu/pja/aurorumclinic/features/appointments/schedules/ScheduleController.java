package pl.edu.pja.aurorumclinic.features.appointments.schedules;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('DOCTOR', 'EMPLOYEE', 'ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("")
    public ResponseEntity<?> createSchedule(@RequestBody @Valid CreateScheduleRequest createScheduleRequest) {
        scheduleService.createSchedule(createScheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
