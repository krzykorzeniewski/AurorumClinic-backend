package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isFullyAuthenticated()")
public class UpdatePassword {

    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;

    @PutMapping("/me/password")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateUserPassword(
            @RequestBody @Valid UpdatePasswordRequest request,
            @AuthenticationPrincipal Long userId) {
        handle(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(UpdatePasswordRequest request, Long userId) {
        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        passwordValidator.validatePassword(request.password);
        userFromDb.setPassword(passwordEncoder.encode(request.password));
    }

    record UpdatePasswordRequest(String password) {
    }

}
