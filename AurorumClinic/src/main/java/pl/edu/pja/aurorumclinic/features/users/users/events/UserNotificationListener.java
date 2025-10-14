package pl.edu.pja.aurorumclinic.features.users.users.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;
    private final SmsService smsService;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddress;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void handlePendingEmailCreatedEvent(PendingEmailCreatedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.EMAIL_UPDATE, 10);

        Context context = new Context();
        context.setVariable("code", token.getRawValue());
        String htmlAsText = springTemplateEngine.process("update-email-email", context);

        emailService.sendEmail(noreplyEmailAddress, user.getPendingEmail(), "Zmiana adresu email",
                htmlAsText);
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void handlePendingPhoneNumberCreatedEvent(PendingPhoneNumberCreatedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.PHONE_NUMBER_UPDATE, 15);
        smsService.sendSms("+48"+ user.getPendingPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny zmiany numeru telefonu w Aurorum Clinic : " + token.getRawValue());
    }

    @EventListener
    @Transactional
    @Async
    public void handleMfaUpdateRequestedEvent(MfaUpdateRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.TWO_FACTOR_AUTH_UPDATE, 10);
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny do ustawienia " +
                        "uwierzytelniania dwuskładnikowego w Aurorum Clinic: " + token.getRawValue());
    }

}
