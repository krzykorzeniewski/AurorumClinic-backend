package pl.edu.pja.aurorumclinic.features.newsletter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.newsletter.events.NewsletterEmailMessageCreated;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.NewsletterMessage;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterScheduler {

    private final PatientRepository patientRepository;
    private final NewsletterMessageRepository newsletterMessageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 0 19 1 * *")
    @Transactional
    public void sendNewsletter() {
        List<Patient> patientsWhoSubscribed = patientRepository.findByNewsletterTrue();
        List<NewsletterMessage> approvedNewsletterMessages = newsletterMessageRepository.findBySentAtNullAndApprovedTrue();
        if (!patientsWhoSubscribed.isEmpty() && !approvedNewsletterMessages.isEmpty()) {
            for (Patient patient: patientsWhoSubscribed) {
               NewsletterMessage messageToBeSent = approvedNewsletterMessages.get(0);
               messageToBeSent.setSentAt(LocalDateTime.now());
               NewsletterEmailMessage newsletterEmailMessage = NewsletterEmailMessage.builder()
                        .subject(messageToBeSent.getSubject())
                        .content(messageToBeSent.getText())
                        .patient(patient)
                        .build();
                applicationEventPublisher.publishEvent(new NewsletterEmailMessageCreated(newsletterEmailMessage));
            }
        }
    }

}
