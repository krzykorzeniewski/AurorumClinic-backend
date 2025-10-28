package pl.edu.pja.aurorumclinic.features.schedules.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDeleteSchedule {

    private final ScheduleRepository scheduleRepository;

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSchedule(@PathVariable("id") Long scheduleId,
                                                         @AuthenticationPrincipal Long doctorId) {
        handle(scheduleId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long scheduleId, Long doctorId) {
        Schedule scheduleFromDb = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(scheduleFromDb.getDoctor().getId(), doctorId)) {
            throw new ApiAuthorizationException("doctor id is not assigned to this schedule");
        }
        scheduleRepository.delete(scheduleFromDb);
    }

}
