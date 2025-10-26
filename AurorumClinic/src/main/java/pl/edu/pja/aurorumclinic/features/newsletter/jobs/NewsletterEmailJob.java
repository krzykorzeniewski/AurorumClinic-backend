package pl.edu.pja.aurorumclinic.features.newsletter.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.newsletter.events.NewsletterEmailMessageCreated;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterEmailMessage;
import pl.edu.pja.aurorumclinic.features.newsletter.shared.NewsletterMessageRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsletterEmailJob {

    private final NewsletterMessageRepository newsletterMessageRepository;
    private final PatientRepository patientRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void execute(Long newsletterMessageId) {
        NewsletterMessage newsletterMessFromDb = newsletterMessageRepository.findById(newsletterMessageId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Patient> patientsWhoSubscribed = patientRepository.findByNewsletterTrue();
        if (patientsWhoSubscribed.isEmpty()) {
            return;
        }

        newsletterMessFromDb.setSentAt(LocalDateTime.now());
        NewsletterEmailMessage newsletterEmailMessage = NewsletterEmailMessage.builder()
                .subject(newsletterMessFromDb.getSubject())
                .content(newsletterMessFromDb.getText())
                .subscribedPatients(patientsWhoSubscribed)
                .build();
        applicationEventPublisher.publishEvent(new NewsletterEmailMessageCreated(newsletterEmailMessage));
    }

}
