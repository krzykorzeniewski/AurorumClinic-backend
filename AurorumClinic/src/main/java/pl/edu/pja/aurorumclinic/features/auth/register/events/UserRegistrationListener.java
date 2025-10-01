package pl.edu.pja.aurorumclinic.features.auth.register.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Component
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final EmailService emailService;
    private final TokenService tokenService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;


    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.user();
        Token emailVerificationtoken = tokenService.createToken(user, TokenName.EMAIL_VERIFICATION, 15);
        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue();

        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        String htmlPageAsText = springTemplateEngine.process("user-registered-email", context);

        emailService.sendEmail(
                noreplyEmailAddres,
                user.getEmail(),
                "Weryfikacja konta",
                htmlPageAsText
        );
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAccountVerifyMessageRequestedEvent(VerifyAccountMessageRequestedEvent event) {
        User user = event.user();
        Token emailVerificationtoken = tokenService.createToken(user, TokenName.EMAIL_VERIFICATION, 15);
        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue();

        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        String htmlPageAsText = springTemplateEngine.process("user-registered-email", context);

        emailService.sendEmail(
                noreplyEmailAddres,
                user.getEmail(),
                "Weryfikacja konta",
                htmlPageAsText
        );
    }

}
