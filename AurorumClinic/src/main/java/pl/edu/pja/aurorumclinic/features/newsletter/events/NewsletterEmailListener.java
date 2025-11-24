package pl.edu.pja.aurorumclinic.features.newsletter.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsletterEmailListener {

    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.backend.newsletter-address}")
    private String newsletterEmailAddress;

    @Value("${mail.frontent.newsletter.unsubscribe-link}")
    private String unsubscribeLink;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onNewsletterEmailMessageCreatedEvent(NewsletterEmailMessageCreated event) {
        String subject = event.newsletterEmailMessage().subject();
        String content = event.newsletterEmailMessage().content();
        List<String> emails = event.newsletterEmailMessage().subscribedPatients().stream().map(Patient::getEmail)
                .toList();

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("content", content);
        context.setVariable("unsubscribeLink", unsubscribeLink);

        String htmlPageAsText = springTemplateEngine.process("newsletter-message-email", context);

        for (String email : emails) {
            emailService.sendEmail(newsletterEmailAddress, email, subject, htmlPageAsText);
        }
    }

}
