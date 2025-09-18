package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth/verify-phone-number-token")
@RequiredArgsConstructor
public class VerifyPhoneNumberTokenHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final SmsService smsService;
    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @PostMapping
    @Transactional
    public ResponseEntity<?> getVerifyPhoneNumberToken(@Valid @RequestBody VerifyPhoneNumberTokenRequest requestDto,
                                                       Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(requestDto.phoneNumber());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new AuthorizationDeniedException("Access denied");
        }
        if (userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is already verified", "phoneNumber");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.PHONE_NUMBER_VERIFICATION, 10);

        smsService.sendSms("+48"+userFromDb.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic : " + token.getRawValue());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record VerifyPhoneNumberTokenRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber) {
    }

}
