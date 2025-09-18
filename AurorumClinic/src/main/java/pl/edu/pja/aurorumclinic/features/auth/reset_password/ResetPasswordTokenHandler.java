package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

@RestController
@RequestMapping("/api/auth/reset-password-token")
@RequiredArgsConstructor
public class ResetPasswordTokenHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @PostMapping
    @Transactional
    public ResponseEntity<?> getResetPasswordToken(@Valid @RequestBody PasswordResetTokenRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        if (!userFromDb.isEmailVerified()) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        Token token = tokenService.createToken(userFromDb, TokenName.PASSWORD_RESET, 15);
        String verificationLink = resetPasswordLink + token.getRawValue();
        emailService.sendEmail(
                "support@aurorumclinic.pl",
                userFromDb.getEmail(),
                "Ustaw nowe hasło",
                "Naciśnij link aby zresetować hasło: " + verificationLink
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record PasswordResetTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
