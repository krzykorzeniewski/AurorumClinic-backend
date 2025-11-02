package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.reset_password.events.ResetPasswordTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordValidator passwordValidator;

    @Override
    public void createResetPasswordToken(ResetPasswordTokenRequest resetPasswordTokenRequest) {
        User userFromDb = userRepository.findByEmail(resetPasswordTokenRequest.email());
        if (userFromDb == null) {
            return;
        }
        if (!userFromDb.isEmailVerified()) {
            return;
        }
        Token token = tokenService.createToken(userFromDb, TokenName.PASSWORD_RESET, 15);
        applicationEventPublisher.publishEvent(new ResetPasswordTokenCreatedEvent(userFromDb, token));
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User userFromDb = userRepository.findByEmail(resetPasswordRequest.email());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        passwordValidator.validatePassword(resetPasswordRequest.password());
        tokenService.validateAndDeleteToken(userFromDb, resetPasswordRequest.token());
        userFromDb.setPassword(passwordEncoder.encode(resetPasswordRequest.password()));
    }
}
