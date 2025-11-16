package pl.edu.pja.aurorumclinic.features.users.users.commands;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.validation.Valid;
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
import pl.edu.pja.aurorumclinic.features.users.users.events.UpdatePhoneNumberTokenCreatedEvent;
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
public class CreateUpdatePhoneNumberToken {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    @Value("${phone-number-verification-token.expiration.minutes}")
    private Integer phoneNumberUpdateTokenExpirationInMinutes;

    @PostMapping("/me/phone-number-update-token")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createUpdatePhoneNumberToken(@AuthenticationPrincipal Long id,
                                                        @Valid @RequestBody UpdateUserPhoneNumberTokenRequest requestDto) {
        handle(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long id, UpdateUserPhoneNumberTokenRequest request) {
        String newNumber = request.phoneNumber();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByPhoneNumber(newNumber)) {
            throw new ApiConflictException("Phone number is already taken", "phoneNumber");
        }
        userFromDb.setPendingPhoneNumber(newNumber);
        Token token = tokenService.createOtpToken(userFromDb, TokenName.PHONE_NUMBER_UPDATE,
                phoneNumberUpdateTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new UpdatePhoneNumberTokenCreatedEvent(userFromDb, token));
    }

    public record UpdateUserPhoneNumberTokenRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber) {
    }

}
