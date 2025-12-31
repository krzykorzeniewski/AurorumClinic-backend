package pl.edu.pja.aurorumclinic.features.absences.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetAllAbsences {

    private final AbsenceRepository absenceRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<DoctorGetAbsenceResponse>>> docGetAllAbsences(
            @AuthenticationPrincipal Long doctorId,
            @PageableDefault Pageable pageable
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, pageable)));
    }

    private Page<DoctorGetAbsenceResponse> handle(Long doctorId, Pageable pageable) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        return absenceRepository.findAllDoctorAbsenceDtos(doctorId, pageable);
    }
}
