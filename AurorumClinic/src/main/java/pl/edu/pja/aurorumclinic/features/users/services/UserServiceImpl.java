package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final SmsService smsService;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Value("${mail.backend.noreply-address}")
    private String fromEmailAddress;

    @Override
    public void sendUpdateEmail(Long id, UpdateUserEmailTokenRequest requestDto) {
        String newEmail = requestDto.email();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByEmail(newEmail)) {
            throw new ApiConflictException("Email is already taken", "email");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.EMAIL_UPDATE, 10);
        userFromDb.setPendingEmail(newEmail);

        emailService.sendEmail(
                fromEmailAddress,
                newEmail,
                "Zmiana adresu email",
                "Twój kod do zmiany adresu email w Aurorum Clinic:  " + token.getRawValue()
        );
    }

    @Override
    public void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("User not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, requestDto.otp());
        userFromDb.setPhoneNumber(userFromDb.getPendingPhoneNumber());
        userFromDb.setPendingPhoneNumber(null);
    }

    @Override
    public void sendUpdateSms(Long id, UpdateUserPhoneNumberTokenRequest requestDto) {
        String newNumber = requestDto.phoneNumber();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByPhoneNumber(newNumber)) {
            throw new ApiConflictException("Phone number is already taken", "phoneNumber");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is not verified", "phoneNumber");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.PHONE_NUMBER_UPDATE, 15);
        userFromDb.setPendingPhoneNumber(newNumber);

        smsService.sendSms("+48"+newNumber, fromPhoneNumber,
                "Kod weryfikacyjny zmiany numeru telefonu w Aurorum Clinic : " + token.getRawValue());
    }

    @Override
    public void updateUserEmail(Long id, UpdateUserEmailRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, requestDto.token());
        userFromDb.setEmail(userFromDb.getPendingEmail());
        userFromDb.setPendingEmail(null);
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
        Token token = tokenService.createOtpToken(userFromDb, TokenName.TWO_FACTOR_AUTH_UPDATE, 10);

        smsService.sendSms("+48"+userFromDb.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny do ustawienia " +
                        "uwierzytelniania dwuskładnikowego w Aurorum Clinic : " + token.getRawValue());
    }

    @Override
    public void updateUser2fa(Long id, UpdateUser2FARequest request) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, request.otp());
        userFromDb.setTwoFactorAuth(true);
    }
}
