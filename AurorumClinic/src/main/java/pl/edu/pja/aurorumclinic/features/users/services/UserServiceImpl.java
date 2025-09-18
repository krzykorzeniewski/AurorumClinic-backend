package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.shared.TokenUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.time.LocalDateTime;
import java.util.Objects;

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
                () -> new ApiNotFoundException("Id not found", "id")
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
        User userFromDb = userRepository.findByIdAndPhoneNumberUpdateToken(id, requestDto.otp());
        if (userFromDb == null) {
            throw new ApiNotFoundException("User not found", "id, token");
        }
        if (userFromDb.getPhoneNumberUpdateExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token is expired", "token");
        }
        userFromDb.setPhoneNumber(userFromDb.getPendingPhoneNumber());
        userFromDb.setPendingPhoneNumber(null);
        userFromDb.setPhoneNumberUpdateToken(null);
        userFromDb.setPhoneNumberUpdateExpiryDate(null);
    }

    @Override
    public void sendUpdateSms(Long id, UpdateUserPhoneNumberTokenRequest requestDto) {
        String newNumber = requestDto.phoneNumber();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByPhoneNumber(newNumber)) {
            throw new ApiException("Phone number is already taken", "phoneNumber");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is not verified", "phoneNumber");
        }
        String otp = tokenUtils.createOtp();
        userFromDb.setPendingPhoneNumber(newNumber);
        userFromDb.setPhoneNumberUpdateToken(otp);
        userFromDb.setPhoneNumberUpdateExpiryDate(LocalDateTime.now().plusMinutes(15));

        smsService.sendSms("+48"+newNumber, fromPhoneNumber,
                "Kod weryfikacyjny zmiany numeru telefonu w Aurorum Clinic : " + otp);
    }

    @Override
    public void updateUserEmail(Long id, UpdateUserEmailRequest requestDto) {
        User userFromDb = userRepository.findByIdAndEmailUpdateToken(id, requestDto.token());
        if (userFromDb == null) {
            throw new ApiNotFoundException("User not found", "id, token");
        }
        if (userFromDb.getEmailUpdateExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token is expired", "token");
        }
        userFromDb.setEmail(userFromDb.getPendingEmail());
        userFromDb.setPendingEmail(null);
        userFromDb.setEmailUpdateToken(null);
        userFromDb.setEmailUpdateExpiryDate(null);
    }

    @Override
    public void send2faSms(Long id, UpdateUser2FATokenRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(userFromDb.getPhoneNumber(), requestDto.phoneNumber())) {
            throw new ApiException("User phone number doest not match", "phoneNumber");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is not verified", "phoneNumber");
        }
        if (userFromDb.isTwoFactorAuth()) {
            throw new ApiException("Phone number already has 2fa enabled", "phoneNumber");
        }
        String otp = tokenUtils.createOtp();
        userFromDb.setTwoFactorAuthUpdateToken(otp);
        userFromDb.setTwoFactorAuthUpdateExpiryDate(LocalDateTime.now().plusMinutes(10));

        smsService.sendSms("+48"+userFromDb.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny do ustawienia " +
                        "uwierzytelniania dwuskładnikowego w Aurorum Clinic : " + otp);
    }

    @Override
    public void updateUser2fa(Long id, UpdateUser2FARequest request) {
        User userFromDb = userRepository.findByIdAndTwoFactorAuthUpdateToken(id, request.otp());
        if (userFromDb == null) {
            throw new ApiNotFoundException("User not found", "id, token");
        }
        if (userFromDb.getTwoFactorAuthUpdateExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token is expired", "token");
        }
        userFromDb.setTwoFactorAuth(true);
        userFromDb.setTwoFactorAuthUpdateToken(null);
        userFromDb.setTwoFactorAuthUpdateExpiryDate(null);
    }
}
