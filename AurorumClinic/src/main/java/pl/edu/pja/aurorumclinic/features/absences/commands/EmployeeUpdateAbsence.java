package pl.edu.pja.aurorumclinic.features.absences.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeUpdateAbsence {

    private final AbsenceRepository absenceRepository;
    private final AbsenceValidator absenceValidator;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> empUpdateAbsence(@PathVariable("id") Long absenceId,
                                                           @RequestBody @Valid EmpUpdateAbsenceRequest request) {
        handle(absenceId, request);
        return ResponseEntity.ok(ApiResponse.success(null));

    }

    private void handle(Long absenceId, EmpUpdateAbsenceRequest request) {
        Absence absenceFromDb = absenceRepository.findById(absenceId).orElseThrow(
                () -> new ApiNotFoundException("absence Id not found", "absenceId")
        );
        absenceValidator.validateNewTimeslot(request.startedAt, request.finishedAt, absenceFromDb.getDoctor(), absenceFromDb);
        absenceFromDb.setName(request.name);
        absenceFromDb.setStartedAt(request.startedAt);
        absenceFromDb.setFinishedAt(request.finishedAt);
    }

    record EmpUpdateAbsenceRequest(@NotNull LocalDateTime startedAt,
                                   @NotNull LocalDateTime finishedAt,
                                   @NotBlank @Size(max = 100) String name) {
    }

}
