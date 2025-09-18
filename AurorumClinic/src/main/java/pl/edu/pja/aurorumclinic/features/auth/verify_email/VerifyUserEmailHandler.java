package pl.edu.pja.aurorumclinic.features.auth.verify_email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.TokenRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/verify-email")
@RequiredArgsConstructor
public class VerifyUserEmailHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @PostMapping
    @Transactional
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (userFromDb.isEmailVerified()) {
            throw new ApiAuthException("Email is already verified", "email");
        }
        tokenService.validateAndDeleteToken(userFromDb, requestDto.token);
        userFromDb.setEmailVerified(true);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record VerifyEmailRequest(@NotBlank @Size(max = 100) String token,
                                     @NotBlank @Email @Size(max = 100) String email) {
    }



}
