package pl.edu.pja.aurorumclinic.features.auth.login;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

@RestController
@RequestMapping("/api/auth/login-mfa-token")
@RequiredArgsConstructor
public class MfaLoginTokenHandler {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> get2faToken(@Valid @RequestBody TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        User userFromDb = userRepository.findByEmail(twoFactorAuthTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiAuthException("Phone number is not verified", "phoneNumber");
        }
        if (!userFromDb.isTwoFactorAuth()) {
            throw new ApiAuthException("Given email has 2fa disabled", "email");
        }
        applicationEventPublisher.publishEvent(new MfaLoginEvent(userFromDb));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record TwoFactorAuthTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
