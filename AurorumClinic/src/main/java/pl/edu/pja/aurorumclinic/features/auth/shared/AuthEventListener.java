package pl.edu.pja.aurorumclinic.features.auth.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.features.auth.login.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.*;
import pl.edu.pja.aurorumclinic.features.auth.reset_password.events.ResetPasswordTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events.PhoneNumberVerificationTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

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
    public void onMfaTokenCreatedEvent(MfaTokenCreatedEvent event) {
        Token token = event.token();
        smsService.sendSms("+48" + event.user().getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + token.getRawValue());
    }

    @Async
    @TransactionalEventListener
    public void onDoctorRegisteredEvent(DoctorRegisteredEvent event) {
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
    public void onEmployeeRegisteredEvent(EmployeeRegisteredEvent event) {
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
    public void onStaffMemberPasswordCreatedEvent(StaffMemberPasswordCreatedEvent event) {
        User employee = event.user();

        Context context = new Context();
        context.setVariable("password", event.password());
        String htmlPageAsText = springTemplateEngine.process("employee-registered-email", context);

        emailService.sendEmail(
                noreplyEmailAddres,
                employee.getEmail(),
                "Twoje hasło zostało wygenerowane",
                htmlPageAsText
        );
    }

    @Async
    @TransactionalEventListener
    public void onPatientRegisteredEvent(PatientRegisteredEvent event) {
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

    @Async
    @EventListener
    public void onEmailVerificationTokenCreatedEvent(EmailVerificationTokenCreatedEvent event) {
        User user = event.user();
        Token emailVerificationtoken = event.token();
        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue() + "?email=" + user.getEmail();

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

    @Async
    @EventListener
    public void onResetPasswordTokenCreatedEvent(ResetPasswordTokenCreatedEvent event) {
        User user = event.user();
        Token token = event.token();
        String resetLink = resetPasswordLink + token.getRawValue() + "?email=" + user.getEmail();

        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String htmlAsText = springTemplateEngine.process("reset-password-email", context);

        emailService.sendEmail(noreplyEmailAddres, user.getEmail(), "Ustaw nowe hasło", htmlAsText);
    }

    @Async
    @EventListener
    public void onPhoneNumVerificationTokenCreatedEvent(PhoneNumberVerificationTokenCreatedEvent event) {
        User user = event.user();
        Token token = event.token();
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic: " + token.getRawValue());
    }

}
