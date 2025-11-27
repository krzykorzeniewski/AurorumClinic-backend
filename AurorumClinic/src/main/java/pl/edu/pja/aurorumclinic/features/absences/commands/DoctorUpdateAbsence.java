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
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorUpdateAbsence {

    private final DoctorRepository doctorRepository;
    private final AbsenceRepository absenceRepository;
    private final AbsenceValidator absenceValidator;

    @PutMapping("/me/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> docUpdateAbsence(@PathVariable("id") Long absenceId,
                                                           @RequestBody @Valid DocUpdateAbsenceRequest request,
                                                           @AuthenticationPrincipal Long doctorId) {
        handle(absenceId, request, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));

    }

    private void handle(Long absenceId, DocUpdateAbsenceRequest request, Long doctorId) {
        Absence absenceFromDb = absenceRepository.findById(absenceId).orElseThrow(
                () -> new ApiNotFoundException("absence Id not found", "absenceId")
        );
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("doctor Id not found", "doctorId")
        );
        if (!Objects.equals(absenceFromDb.getDoctor().getId(), doctorFromDb.getId())) {
            throw new ApiAuthorizationException("Absence doctor id does not match request doctor id");
        }
        absenceValidator.validateNewTimeslot(request.startedAt, request.finishedAt, doctorFromDb, absenceFromDb);
        absenceFromDb.setName(request.name);
        absenceFromDb.setStartedAt(request.startedAt);
        absenceFromDb.setFinishedAt(request.finishedAt);
    }

    record DocUpdateAbsenceRequest(@NotNull LocalDateTime startedAt,
                                   @NotNull LocalDateTime finishedAt,
                                   @NotBlank @Size(max = 100) String name) {
    }
}
