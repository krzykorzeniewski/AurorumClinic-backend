package pl.edu.pja.aurorumclinic.features.auth.login.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Component
@RequiredArgsConstructor
public class LoginListener {

    private final TokenService tokenService;
    private final SmsService smsService;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @EventListener
    @Transactional
    public void handleMfaLoginAttemptedEvent(MfaLoginRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.TWO_FACTOR_AUTH, 1);
        System.err.println(token.getRawValue());
        smsService.sendSms("+48" + user.getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + token.getRawValue());
    }

}
