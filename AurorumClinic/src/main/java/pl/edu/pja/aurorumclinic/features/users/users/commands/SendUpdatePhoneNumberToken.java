package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
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
import pl.edu.pja.aurorumclinic.features.users.users.events.PendingPhoneNumberCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
public class SendUpdatePhoneNumberToken {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/me/phone-number-update-token")
    @Transactional
    public ResponseEntity<?> updateUserPhoneNumberToken(@AuthenticationPrincipal Long id,
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
        applicationEventPublisher.publishEvent(new PendingPhoneNumberCreatedEvent(userFromDb));
    }

    public record UpdateUserPhoneNumberTokenRequest(@Size(min = 9, max = 9) @NotBlank String phoneNumber) {
    }

}
