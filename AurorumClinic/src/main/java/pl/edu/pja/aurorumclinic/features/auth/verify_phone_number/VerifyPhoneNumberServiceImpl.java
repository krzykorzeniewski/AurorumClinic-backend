package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events.PhoneNumberVerificationTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
public class VerifyPhoneNumberServiceImpl implements VerifyPhoneNumberService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void createPhoneNumberVerificationToken(Long id) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("id not found", "id")
        );
        if (userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is already verified", "phoneNumber");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.PHONE_NUMBER_VERIFICATION, 10);
        applicationEventPublisher.publishEvent(new PhoneNumberVerificationTokenCreatedEvent(userFromDb, token));
    }

    @Override
    @Transactional
    public void verifyPhoneNumber(VerifyPhoneNumberRequest verifyPhoneNumberRequest, Long id) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("id not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, verifyPhoneNumberRequest.token());
        userFromDb.setPhoneNumberVerified(true);
    }
}
