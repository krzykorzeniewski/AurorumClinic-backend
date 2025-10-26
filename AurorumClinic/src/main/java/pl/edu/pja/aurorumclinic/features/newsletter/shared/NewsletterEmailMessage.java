package pl.edu.pja.aurorumclinic.features.newsletter.shared;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.util.List;

@Builder
public record NewsletterEmailMessage(String subject,
                                     String content,
                                     List<Patient> subscribedPatients) {
}
