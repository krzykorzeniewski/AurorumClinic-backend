package pl.edu.pja.aurorumclinic.features.auth.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.features.auth.login.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.AccountVerificationRequestedEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.DoctorRegisteredEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.EmployeeRegisteredEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.PatientRegisteredEvent;
import pl.edu.pja.aurorumclinic.features.auth.reset_password.events.ResetPasswordRequestedEvent;
import pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events.PhoneNumberVerificationRequestedEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final TokenService tokenService;
    private final SmsService smsService;
    private final EmailService emailService;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @Async
    @EventListener
    public void handleMfaLoginAttemptedEvent(MfaTokenCreatedEvent event) {
        Token token = event.token();
        smsService.sendSms("+48" + event.user().getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + token.getRawValue());
    }

    @Async
    @TransactionalEventListener
    public void handleDoctorRegisteredEvent(DoctorRegisteredEvent event) {
        Doctor doctor = event.doctor();

        Context context = new Context();
        context.setVariable("password", event.password());
        String htmlPageAsText = springTemplateEngine.process("employee-registered-email", context);
        emailService.sendEmail(
                noreplyEmailAddres,
                doctor.getEmail(),
                "Twoje konto zostało utworzone",
                htmlPageAsText
        );
    }

    @Async
    @TransactionalEventListener
    public void handleEmployeeRegisteredEvent(EmployeeRegisteredEvent event) {
        User employee = event.user();

        Context context = new Context();
        context.setVariable("password", event.password());
        String htmlPageAsText = springTemplateEngine.process("employee-registered-email", context);

        emailService.sendEmail(
                noreplyEmailAddres,
                employee.getEmail(),
                "Twoje konto zostało utworzone",
                htmlPageAsText
        );
    }

    @Async
    @TransactionalEventListener
    public void handleUserRegisteredEvent(PatientRegisteredEvent event) {
        User user = event.user();
        Token emailVerificationToken = event.token();
        String verificationLink = mailVerificationLink + emailVerificationToken.getRawValue();

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
    @Transactional
    @Async
    public void handleAccountVerifyMessageRequestedEvent(AccountVerificationRequestedEvent event) {
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
    @Transactional
    @Async
    public void handleResetPasswordMessageRequestEvent(ResetPasswordRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createToken(user, TokenName.PASSWORD_RESET, 15);
        String resetLink = resetPasswordLink + token.getRawValue();

        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String htmlAsText = springTemplateEngine.process("reset-password-email", context);

        emailService.sendEmail(noreplyEmailAddres, user.getEmail(), "Ustaw nowe hasło", htmlAsText);
    }

    @EventListener
    @Transactional
    @Async
    public void handleVerifyPhoneNumberRequestedEvent(PhoneNumberVerificationRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.PHONE_NUMBER_VERIFICATION, 10);
        System.err.println(token.getRawValue());
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic: " + token.getRawValue());
    }

}
