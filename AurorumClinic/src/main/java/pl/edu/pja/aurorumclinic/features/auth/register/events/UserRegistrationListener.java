package pl.edu.pja.aurorumclinic.features.auth.register.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
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

    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.user();
        Token emailVerificationtoken = tokenService.createToken(user, TokenName.EMAIL_VERIFICATION, 15);
        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue();

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                user.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAccountVerifyMessageRequestedEvent(VerifyAccountMessageRequestedEvent event) {
        User user = event.user();
        Token emailVerificationtoken = tokenService.createToken(user, TokenName.EMAIL_VERIFICATION, 15);
        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue();

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                user.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
    }

}
