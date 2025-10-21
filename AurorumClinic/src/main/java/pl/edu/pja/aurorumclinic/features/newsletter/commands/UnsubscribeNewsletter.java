package pl.edu.pja.aurorumclinic.features.newsletter.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
@PreAuthorize("hasRole('PATIENT')")
public class UnsubscribeNewsletter {

    private final PatientRepository patientRepository;

    @PostMapping("/unsubscribe")
    @Transactional
    public ResponseEntity<ApiResponse<?>> unsubscribeNewsletter(@AuthenticationPrincipal Long patientId) {
        handle(patientId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long patientId) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!patientFromDb.isNewsletter()) {
            throw new ApiException("Email is not subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(false);
    }

}
