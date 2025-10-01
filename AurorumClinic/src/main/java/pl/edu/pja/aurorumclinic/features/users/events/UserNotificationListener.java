package pl.edu.pja.aurorumclinic.features.users.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class UserNotificationListener {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddress;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePendingEmailCreatedEvent(PendingEmailCreatedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.EMAIL_UPDATE, 10);

        Context context = new Context();
        context.setVariable("code", token.getRawValue());
        String htmlAsText = springTemplateEngine.process("update-email-email", context);

        emailService.sendEmail(noreplyEmailAddress, user.getPendingEmail(), "Zmiana adresu email",
                htmlAsText);
    }

}
