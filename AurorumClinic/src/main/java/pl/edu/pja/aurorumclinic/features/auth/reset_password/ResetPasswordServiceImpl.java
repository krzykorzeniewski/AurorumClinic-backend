package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
@Transactional
public class ResetPasswordServiceImpl implements ResetPasswordService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @Override
    public void sendResetPasswordEmail(ResetPasswordTokenRequest resetPasswordTokenRequest) {
        User userFromDb = userRepository.findByEmail(resetPasswordTokenRequest.email());
        if (userFromDb == null) {
            return;
        }
        if (!userFromDb.isEmailVerified()) {
            return;
        }
        Token token = tokenService.createToken(userFromDb, TokenName.PASSWORD_RESET, 15);
        String verificationLink = resetPasswordLink + token.getRawValue();
        emailService.sendEmail(
                "support@aurorumclinic.pl",
                userFromDb.getEmail(),
                "Ustaw nowe hasło",
                "Naciśnij link aby zresetować hasło: " + verificationLink
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User userFromDb = userRepository.findByEmail(resetPasswordRequest.email());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        tokenService.validateAndDeleteToken(userFromDb, resetPasswordRequest.token());
        userFromDb.setPassword(passwordEncoder.encode(resetPasswordRequest.password()));
    }
}
