package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.events.PendingEmailCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
public class MeSendUpdateEmailToken {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/me/email-update-token")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateUserEmailToken(@AuthenticationPrincipal Long id,
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
        applicationEventPublisher.publishEvent(new PendingEmailCreatedEvent(userFromDb));
    }

    public record UpdateUserEmailTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
