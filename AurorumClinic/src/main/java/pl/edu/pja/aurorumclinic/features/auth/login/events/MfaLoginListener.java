package pl.edu.pja.aurorumclinic.features.auth.login.events;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

@Component
@RequiredArgsConstructor
public class MfaLoginListener {

    private final TokenService tokenService;
    private final SmsService smsService;
    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @EventListener
    public void handleMfaLoginEvent(MfaLoginEvent mfaLoginEvent) {
        User user = (User) mfaLoginEvent.getSource();
        Token token = tokenService.createOtpToken(user, TokenName.TWO_FACTOR_AUTH, 5);
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + token.getRawValue());
    }
}
