package pl.edu.pja.aurorumclinic.features.users.users.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;
    private final SmsService smsService;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddress;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Async
    @TransactionalEventListener
    public void onUpdateEmailTokenCreated(UpdateEmailTokenCreatedEvent event) {
        User user = event.user();
        Token token = event.token();
        Context context = new Context();
        context.setVariable("code", token.getRawValue());
        String htmlAsText = springTemplateEngine.process("update-email-email", context);

        emailService.sendEmail(noreplyEmailAddress, user.getPendingEmail(), "Zmiana adresu email",
                htmlAsText);
    }

    @Async
    @TransactionalEventListener
    public void onUpdatePhoneNumberTokenCreated(UpdatePhoneNumberTokenCreatedEvent event) {
        User user = event.user();
        Token token = event.token();
        smsService.sendSms("+48"+ user.getPendingPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny zmiany numeru telefonu w Aurorum Clinic : " + token.getRawValue());
    }

    @Async
    @EventListener
    public void onMfaTokenCreated(MfaTokenCreatedEvent event) {
        User user = event.user();
        Token token = event.token();
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny do ustawienia " +
                        "uwierzytelniania dwuskładnikowego w Aurorum Clinic: " + token.getRawValue());
    }

}
