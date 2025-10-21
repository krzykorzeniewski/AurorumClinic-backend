package pl.edu.pja.aurorumclinic.features.newsletter.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
@PreAuthorize("hasRole('PATIENT')")
public class SubscribeNewsletter {

    private final PatientRepository patientRepository;

    @PostMapping("/subscribe")
    @Transactional
    public ResponseEntity<ApiResponse<?>> subscribeToNewsletter(@RequestBody @Valid SubscribeRequest request,
                                                                @AuthenticationPrincipal Long patientId) {
        handle(request, patientId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(SubscribeRequest request, Long patientId) {
        Patient patientFromDb = patientRepository.findByEmail(request.email());
        if (patientFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (!Objects.equals(patientFromDb.getId(), patientId)) {
            throw new ApiAuthorizationException("Current patient id doest not match the one from db");
        }
        if (patientFromDb.isNewsletter()) {
            throw new ApiException("Email is already subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(true);
    }

    record SubscribeRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
