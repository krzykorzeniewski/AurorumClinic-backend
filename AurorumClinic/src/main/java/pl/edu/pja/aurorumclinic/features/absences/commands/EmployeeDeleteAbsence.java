package pl.edu.pja.aurorumclinic.features.absences.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeDeleteAbsence {

    private final AbsenceRepository absenceRepository;

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> empDeleteAbsence(@PathVariable("id") Long absenceId) {
        handle(absenceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long absenceId) {
        absenceRepository.findById(absenceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        absenceRepository.deleteById(absenceId);
    }

}
