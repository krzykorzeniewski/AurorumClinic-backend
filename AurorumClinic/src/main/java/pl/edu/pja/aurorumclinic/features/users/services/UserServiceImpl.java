package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailTokenRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;
import pl.edu.pja.aurorumclinic.shared.TokenUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenUtils tokenUtils;
    private final SmsService smsService;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Value("${mail.backend.noreply-address}")
    private String fromEmailAddress;

    @Value("${mail.frontend.email-update-link}")
    private String mailUpdateLink;

    @Override
    public void sendUpdateEmail(Long id, UpdateUserEmailTokenRequest requestDto) {
        String newEmail = requestDto.email();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiException("Id not found", "id")
        );
        if (userRepository.existsByEmail(newEmail)) {
            throw new ApiException("Email is already taken", "email");
        }
        String token = tokenUtils.createRandomToken();
        userFromDb.setEmailUpdateToken(token);
        userFromDb.setEmailUpdateExpiryDate(LocalDateTime.now().plusMinutes(15));
        userFromDb.setPendingEmail(newEmail);

        String verificationLink = mailUpdateLink + token;

        emailService.sendEmail(
                fromEmailAddress,
                newEmail,
                "Zmiana adresu email",
                "Naciśnij link aby zmienić adres email: " + verificationLink
        );
    }

    @Override
    public void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
    }

    @Override
    public void updateUserEmail(Long id, UpdateUserEmailRequest requestDto) {
        User userFromDb = userRepository.findByIdAndEmailUpdateToken(id, requestDto.token());
        if (userFromDb == null) {
            throw new ApiException("User not found", "id, token");
        }
        if (userFromDb.getEmailUpdateExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token is expired", "token");
        }
        userFromDb.setEmail(userFromDb.getPendingEmail());
        userFromDb.setPendingEmail(null);
        userFromDb.setEmailUpdateToken(null);
        userFromDb.setEmailUpdateExpiryDate(null);
    }
}
