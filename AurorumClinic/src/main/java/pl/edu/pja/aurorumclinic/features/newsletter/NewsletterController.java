package pl.edu.pja.aurorumclinic.features.newsletter;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
@PermitAll
public class NewsletterController {

    private final PatientRepository patientRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody @Valid SubscribeRequest subscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(subscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (patientFromDb.isNewsletter()) {
            throw new ApiException("Email is already subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(true);
        patientRepository.save(patientFromDb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody @Valid UnsubscribeRequest unsubscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(unsubscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (!patientFromDb.isNewsletter()) {
            throw new ApiException("Email is not subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(false);
        patientRepository.save(patientFromDb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
