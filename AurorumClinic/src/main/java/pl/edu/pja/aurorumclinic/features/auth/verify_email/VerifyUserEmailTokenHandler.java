package pl.edu.pja.aurorumclinic.features.auth.verify_email;

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
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

@RestController
@RequestMapping("/api/auth/verify-email-token")
@RequiredArgsConstructor
public class VerifyUserEmailTokenHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @PostMapping
    @Transactional
    public ResponseEntity<?> getVerifyEmailToken(@Valid @RequestBody VerifyEmailTokenRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (userFromDb.isEmailVerified()) {
            throw new ApiException("Email is already verified", "email");
        }
        Token emailVerificationtoken = tokenService.createToken(userFromDb, TokenName.EMAIL_VERIFICATION, 15);

        String verificationLink = mailVerificationLink + emailVerificationtoken.getRawValue();

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                userFromDb.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record VerifyEmailTokenRequest(@NotBlank @Email @Size(max = 100) String email) {
    }

}
