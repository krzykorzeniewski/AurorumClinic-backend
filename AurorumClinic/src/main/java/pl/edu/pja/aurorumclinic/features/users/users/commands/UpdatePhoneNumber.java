package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
public class UpdatePhoneNumber {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @PutMapping("/me/phone-number")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateUserPhoneNumber(@AuthenticationPrincipal Long id,
                                                   @Valid @RequestBody UpdateUserPhoneNumberRequest requestDto) {
        handle(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long id, UpdateUserPhoneNumberRequest request) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("User not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, request.token());
        userFromDb.setPhoneNumber(userFromDb.getPendingPhoneNumber());
        userFromDb.setPendingPhoneNumber(null);
    }

    public record UpdateUserPhoneNumberRequest(@Size(min = 6, max = 6) @NotBlank String token) {
    }

}
