package pl.edu.pja.aurorumclinic.features.absences.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class DoctorGetAbsenceById {

    private final DoctorRepository doctorRepository;
    private final AbsenceRepository absenceRepository;

    @GetMapping("/me/{id}")
    public ResponseEntity<ApiResponse<DoctorGetAbsenceResponse>> docGetAbsenceById(@PathVariable("id") Long absenceId,
                                                                           @AuthenticationPrincipal Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(handle(absenceId, doctorId)));
    }

    private DoctorGetAbsenceResponse handle(Long absenceId, Long doctorId) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        return absenceRepository.findDoctorAbsenceDtoById(absenceId, doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "absenceId")
        );
    }

}
