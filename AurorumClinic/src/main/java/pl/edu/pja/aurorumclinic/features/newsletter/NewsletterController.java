package pl.edu.pja.aurorumclinic.features.newsletter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

@RestController
@RequestMapping("/api/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final PatientRepository patientRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe() {
        return ResponseEntity.ok().build();
    }

}
