package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class GetDoctorByIdAbsences {

    private final DoctorRepository doctorRepository;
    private final AbsenceRepository absenceRepository;

    @GetMapping("/{id}/absences")
    public ResponseEntity<ApiResponse<List<GetDoctorAbsenceResponse>>> getDoctorAbsences(
            @PathVariable("id") Long doctorId,
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt)));
    }

    private List<GetDoctorAbsenceResponse> handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        List<Absence> absencesFromDb = absenceRepository.findAllByDoctorIdAndBetween
                (doctorId, startedAt, finishedAt);
        return absencesFromDb.stream().map(absence -> GetDoctorAbsenceResponse.builder()
                    .id(absence.getId())
                    .name(absence.getName())
                    .startedAt(absence.getStartedAt())
                    .finishedAt(absence.getFinishedAt())
                .build()).toList();
    }

    @Builder
    public record GetDoctorAbsenceResponse(Long id,
                                     String name,
                                     LocalDateTime startedAt,
                                     LocalDateTime finishedAt) {
    }
}
