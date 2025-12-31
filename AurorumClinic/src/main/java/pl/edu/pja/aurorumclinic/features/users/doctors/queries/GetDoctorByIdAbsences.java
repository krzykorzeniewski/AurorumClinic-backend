package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class GetDoctorByIdAbsences {

    private final DoctorRepository doctorRepository;
    private final AbsenceRepository absenceRepository;

    @GetMapping("/{id}/absences")
    public ResponseEntity<ApiResponse<Page<GetDoctorAbsenceResponse>>> getDoctorAbsences(
            @PathVariable("id") Long doctorId,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, pageable)));
    }

    private Page<GetDoctorAbsenceResponse> handle(Long doctorId, Pageable pageable) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        Page<Absence> absencesFromDb = absenceRepository.findAllByDoctorId(doctorId, pageable);
        return absencesFromDb.map(absence -> GetDoctorAbsenceResponse.builder()
                    .id(absence.getId())
                    .name(absence.getName())
                    .startedAt(absence.getStartedAt())
                    .finishedAt(absence.getFinishedAt())
                .build());
    }

    @Builder
    public record GetDoctorAbsenceResponse(Long id,
                                     String name,
                                     LocalDateTime startedAt,
                                     LocalDateTime finishedAt) {
    }
}
