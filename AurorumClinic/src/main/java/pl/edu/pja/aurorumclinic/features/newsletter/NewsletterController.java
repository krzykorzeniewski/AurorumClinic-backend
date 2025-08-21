package pl.edu.pja.aurorumclinic.features.newsletter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final PatientRepository patientRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscribeRequest subscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(subscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiException("Email not found", "email");
        }
        if (patientFromDb.isNewsletter()) {
            throw new ApiException("Email is already subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(true);
        patientRepository.save(patientFromDb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody UnsubscribeRequest unsubscribeRequest) {
        Patient patientFromDb = patientRepository.findByEmail(unsubscribeRequest.email());
        if (patientFromDb == null) {
            throw new ApiException("Email not found", "email");
        }
        if (!patientFromDb.isNewsletter()) {
            throw new ApiException("Email is not subscribed to newsletter", "email");
        }
        patientFromDb.setNewsletter(false);
        patientRepository.save(patientFromDb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
