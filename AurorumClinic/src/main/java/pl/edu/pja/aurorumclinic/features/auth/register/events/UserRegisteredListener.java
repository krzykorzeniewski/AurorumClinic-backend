package pl.edu.pja.aurorumclinic.features.auth.register.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final TokenService tokenService;
    private final EmailService emailService;
    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent userRegisteredEvent) {
        User user = (User) userRegisteredEvent.getSource();
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
