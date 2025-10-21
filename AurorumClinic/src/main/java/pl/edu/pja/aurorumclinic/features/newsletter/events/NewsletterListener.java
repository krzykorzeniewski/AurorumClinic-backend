package pl.edu.pja.aurorumclinic.features.newsletter.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

@Component
@RequiredArgsConstructor
public class NewsletterListener {

    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.backend.newsletter-address}")
    private String newsletterEmailAddress;

    @Value("${mail.frontent.newsletter.unsubscribe-link}")
    private String unsubscribeLink;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleNewsletterEmailMessageCreatedEvent(NewsletterEmailMessageCreated event) {
        String subject = event.newsletterEmailMessage().subject();
        String content = event.newsletterEmailMessage().content();
        String email = event.newsletterEmailMessage().patient().getEmail();

        Context context = new Context();
        context.setVariable("subject", subject);
        context.setVariable("content", content);
        context.setVariable("unsubscribeLink", unsubscribeLink);

        String htmlPageAsText = springTemplateEngine.process("newsletter-message-email", context);
        emailService.sendEmail(newsletterEmailAddress, email, subject, htmlPageAsText);
    }

}
