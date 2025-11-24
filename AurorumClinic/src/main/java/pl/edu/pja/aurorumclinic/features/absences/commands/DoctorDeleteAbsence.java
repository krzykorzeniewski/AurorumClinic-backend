package pl.edu.pja.aurorumclinic.features.absences.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDeleteAbsence {

    private final AbsenceRepository absenceRepository;

    @DeleteMapping("/me/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> docDeleteAbsence(@PathVariable("id") Long absenceId,
                                                           @AuthenticationPrincipal Long doctorId) {
        handle(absenceId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long absenceId, Long doctorId) {
        Absence absenceFromDb = absenceRepository.findById(absenceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(absenceFromDb.getDoctor().getId(), doctorId)) {
            throw new ApiAuthorizationException("Absence doctor id does not match logged in user id");
        }
        absenceRepository.deleteById(absenceId);
    }

}
