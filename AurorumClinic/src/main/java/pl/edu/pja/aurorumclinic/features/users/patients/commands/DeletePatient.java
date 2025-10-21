package pl.edu.pja.aurorumclinic.features.users.patients.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class DeletePatient {

    private final PatientRepository patientRepository;

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional
    public ResponseEntity<ApiResponse<?>> deleteAccount(@AuthenticationPrincipal Long id) {
        handle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long id) {
        Patient patientFromDb = patientRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        patientRepository.delete(patientFromDb);
    }

}
