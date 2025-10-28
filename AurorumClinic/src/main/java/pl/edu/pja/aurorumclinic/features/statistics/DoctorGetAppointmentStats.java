package pl.edu.pja.aurorumclinic.features.statistics;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetAppointmentStats {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorAppointmentStatsResponse>> getDoctorAppointmentStats(
            @AuthenticationPrincipal Long doctorId,
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt)));
    }

    private DoctorAppointmentStatsResponse handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        return appointmentRepository.getDoctorAppointmentStatistics(doctorId, startedAt,
                finishedAt);
    }


}
