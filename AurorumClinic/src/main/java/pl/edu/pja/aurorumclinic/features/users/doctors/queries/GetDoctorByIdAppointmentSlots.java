package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetDoctorByIdAppointmentSlots {

    private final DoctorRepository doctorRepository;

    @PermitAll
    @GetMapping("/{id}/appointment-slots")
    public ResponseEntity<ApiResponse<List<LocalDateTime>>> getAppointmentSlots(@PathVariable Long id,
                                                                   @RequestParam LocalDateTime startedAt,
                                                                   @RequestParam LocalDateTime finishedAt,
                                                                   @RequestParam Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success(handle(id, startedAt, finishedAt, serviceId)));
    }

    private List<LocalDateTime> handle(Long id, LocalDateTime startedAt, LocalDateTime finishedAt, Long serviceId) {
        doctorRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        return doctorRepository.appointmentSlots(startedAt, finishedAt, serviceId, id)
                .stream()
                .map(Timestamp::toLocalDateTime)
                .toList();
    }

}
