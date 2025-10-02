package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.reset_password.events.ResetPasswordRequestedEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void sendResetPasswordEmail(ResetPasswordTokenRequest resetPasswordTokenRequest) {
        User userFromDb = userRepository.findByEmail(resetPasswordTokenRequest.email());
        if (userFromDb == null) {
            return;
        }
        if (!userFromDb.isEmailVerified()) {
            return;
        }
        applicationEventPublisher.publishEvent(new ResetPasswordRequestedEvent(userFromDb));
    }

    @Transactional
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
