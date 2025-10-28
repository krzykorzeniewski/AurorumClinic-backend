package pl.edu.pja.aurorumclinic.features.schedules.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class DeleteSchedule {

    private final ScheduleRepository scheduleRepository;

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSchedule(@PathVariable("id") Long scheduleId) {
        handle(scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long scheduleId) {

    }

}
