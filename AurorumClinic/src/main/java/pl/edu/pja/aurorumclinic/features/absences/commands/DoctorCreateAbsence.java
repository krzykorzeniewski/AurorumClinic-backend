package pl.edu.pja.aurorumclinic.features.absences.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorCreateAbsence {

    private final DoctorRepository doctorRepository;
    private final AbsenceRepository absenceRepository;
    private final AbsenceValidator absenceValidator;

    @PostMapping("/me")
    @Transactional
    public ResponseEntity<ApiResponse<?>> docCreateAbsence(@RequestBody @Valid DocCreateAbsenceRequest request,
                                                           @AuthenticationPrincipal Long doctorId) {
        handle(request, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(DocCreateAbsenceRequest request, Long doctorId) {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        absenceValidator.validateTimeslot(request.startedAt, request.finishedAt, doctorFromDb);
        absenceRepository.save(
                Absence.builder()
                        .doctor(doctorFromDb)
                        .startedAt(request.startedAt)
                        .finishedAt(request.finishedAt)
                        .name(request.name)
                        .build()
        );
    }

    record DocCreateAbsenceRequest(@NotNull LocalDateTime startedAt,
                                   @NotNull LocalDateTime finishedAt,
                                   @NotBlank @Size(max = 100) String name){
    }
}
