package pl.edu.pja.aurorumclinic.features.absences.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeCreateAbsence {

    private final AbsenceRepository absenceRepository;
    private final AbsenceValidator absenceValidator;
    private final DoctorRepository doctorRepository;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> empCreateAbsence(@RequestBody @Valid EmpCreateAbsenceRequest request) {
        handle(request);
        return ResponseEntity.ok(ApiResponse.success(null));

    }

    private void handle(EmpCreateAbsenceRequest request) {
        Doctor doctorFromDb = doctorRepository.findById(request.doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        absenceValidator.validateTimeslot(request.startedAt, request.finishedAt, doctorFromDb);
        absenceRepository.save(Absence.builder()
                        .doctor(doctorFromDb)
                        .name(request.name)
                        .startedAt(request.startedAt)
                        .finishedAt(request.finishedAt)
                .build());
    }

    record EmpCreateAbsenceRequest(@NotNull LocalDateTime startedAt,
                                   @NotNull LocalDateTime finishedAt,
                                   @NotBlank @Size(max = 100) String name,
                                   @NotNull Long doctorId) {
    }

}
