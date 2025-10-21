package pl.edu.pja.aurorumclinic.features.newsletter;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

@Builder
public record NewsletterEmailMessage(String subject,
                                     String content,
                                     Patient patient) {
}
