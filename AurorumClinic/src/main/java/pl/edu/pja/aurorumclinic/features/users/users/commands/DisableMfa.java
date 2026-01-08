package pl.edu.pja.aurorumclinic.features.users.users.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DisableMfa {

    private final UserRepository userRepository;

    @PostMapping("/me/2fa/disable")
    @Transactional
    public ResponseEntity<ApiResponse<?>> disableMfa(@AuthenticationPrincipal Long userId) {
        handle(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long userId) {
        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("User id not found", "userId")
        );
        if (userFromDb.isTwoFactorAuth()) {
            userFromDb.setTwoFactorAuth(false);
        } else {
            throw new ApiException("User has mfa disabled", "userId");
        }
    }

}
