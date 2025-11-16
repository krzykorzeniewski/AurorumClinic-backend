package pl.edu.pja.aurorumclinic.features.users.users.commands;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.events.UpdateEmailTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
@RateLimiting(name = "sensitive")
public class CreateUpdateEmailToken {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    @Value("${email-verification-token.expiration.minutes}")
    private Integer emailUpdateTokenExpirationInMinutes;

    @PostMapping("/me/email-update-token")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createUpdateEmailToken(@AuthenticationPrincipal Long id,
                                                             @Valid @RequestBody UpdateUserEmailTokenRequest requestDto) {
        handle(requestDto, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(UpdateUserEmailTokenRequest request, Long id) {
        String newEmail = request.email();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByEmail(newEmail)) {
            throw new ApiConflictException("Email is already taken", "email");
        }
        userFromDb.setPendingEmail(newEmail);
        Token token = tokenService.createOtpToken(userFromDb, TokenName.EMAIL_UPDATE, emailUpdateTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new UpdateEmailTokenCreatedEvent(userFromDb, token));
    }

    public record UpdateUserEmailTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
