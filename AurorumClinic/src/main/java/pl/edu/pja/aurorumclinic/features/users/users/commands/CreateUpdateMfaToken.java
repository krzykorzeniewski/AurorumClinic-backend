package pl.edu.pja.aurorumclinic.features.users.users.commands;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
@RateLimiting(name = "sensitive")
public class CreateUpdateMfaToken {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    @Value("${2fa-update-token.expiration.minutes}")
    private Integer mfaUpdateTokenExpirationInMinutes;

    @PostMapping("/me/2fa-token")
    public ResponseEntity<ApiResponse<?>> createMfaUpdateToken(@AuthenticationPrincipal Long id) {
        handle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long id) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is not verified", "phoneNumber");
        }
        if (userFromDb.isTwoFactorAuth()) {
            throw new ApiException("Phone number already has 2fa enabled", "phoneNumber");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.TWO_FACTOR_AUTH_UPDATE,
                mfaUpdateTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new MfaTokenCreatedEvent(userFromDb, token));
    }

}
