package pl.edu.pja.aurorumclinic.features.statistics;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stats/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAppointmentStats {

    private final AppointmentRepository appointmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<GetAppointmentStatsResponse>> getAppointmentStatistics(
            @RequestParam(required = false) LocalDateTime startedAt,
            @RequestParam(required = false) LocalDateTime finishedAt
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt)));
    }

    private GetAppointmentStatsResponse handle(LocalDateTime startedAt, LocalDateTime finishedAt) {
        if (startedAt != null && finishedAt != null) {
            return appointmentRepository.getAllAppointmentStatisticsBetween(startedAt, finishedAt);
        }
        return appointmentRepository.getAllAppointmentStatistics();
    }

}
