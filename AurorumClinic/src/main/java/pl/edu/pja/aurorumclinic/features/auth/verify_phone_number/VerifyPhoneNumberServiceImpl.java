package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class VerifyPhoneNumberServiceImpl implements VerifyPhoneNumberService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SmsService smsService;
    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Override
    public void sendVerifyPhoneNumberSms(VerifyPhoneNumberTokenRequest verifyPhoneNumberTokenRequest, Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(verifyPhoneNumberTokenRequest.phoneNumber());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new ApiAuthorizationException("Access denied");
        }
        if (userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is already verified", "phoneNumber");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.PHONE_NUMBER_VERIFICATION, 10);

        smsService.sendSms("+48"+userFromDb.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic : " + token.getRawValue());
    }

    @Override
    public void verifyPhoneNumber(VerifyPhoneNumberRequest verifyPhoneNumberRequest, Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(verifyPhoneNumberRequest.phoneNumber());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new ApiAuthorizationException("Access denied");
        }
        tokenService.validateAndDeleteToken(userFromDb, verifyPhoneNumberRequest.token());
        userFromDb.setPhoneNumberVerified(true);
    }
}
