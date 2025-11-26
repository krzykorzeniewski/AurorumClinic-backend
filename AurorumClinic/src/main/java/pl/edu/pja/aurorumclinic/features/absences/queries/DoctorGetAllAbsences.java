package pl.edu.pja.aurorumclinic.features.absences.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetAllAbsences {

    private final AbsenceRepository absenceRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<DoctorGetAbsenceResponse>>> docGetAllAbsences(
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt,
            @AuthenticationPrincipal Long doctorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt, doctorId)));
    }

    private List<DoctorGetAbsenceResponse> handle(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        return absenceRepository.findAllDoctorAbsenceDtosBetween(startedAt, finishedAt, doctorId);
    }
}
