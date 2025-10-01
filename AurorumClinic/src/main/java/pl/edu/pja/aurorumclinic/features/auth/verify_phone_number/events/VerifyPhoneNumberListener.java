package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events;

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
public class VerifyPhoneNumberListener {

    private final TokenService tokenService;
    private final SmsService smsService;
    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @EventListener
    @Transactional
    public void handleVerifyPhoneNumberRequestedEvent(PhoneNumberVerificationRequestedEvent event) {
        User user = event.user();
        Token token = tokenService.createOtpToken(user, TokenName.PHONE_NUMBER_VERIFICATION, 10);
        System.err.println(token.getRawValue());
        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic: " + token.getRawValue());
    }
}
