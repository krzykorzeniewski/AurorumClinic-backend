package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.users.events.MfaUpdateRequestedEvent;
import pl.edu.pja.aurorumclinic.features.users.events.PendingEmailCreatedEvent;
import pl.edu.pja.aurorumclinic.features.users.events.PendingPhoneNumberCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Override
    @Transactional
    public void sendUpdateEmail(Long id, UpdateUserEmailTokenRequest requestDto) {
        String newEmail = requestDto.email();
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (userRepository.existsByEmail(newEmail)) {
            throw new ApiConflictException("Email is already taken", "email");
        }
        userFromDb.setPendingEmail(newEmail);
        applicationEventPublisher.publishEvent(new PendingEmailCreatedEvent(userFromDb));
    }

    @Override
    @Transactional
    public void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("User not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, requestDto.token());
        userFromDb.setPhoneNumber(userFromDb.getPendingPhoneNumber());
        userFromDb.setPendingPhoneNumber(null);
    }

    @Override
    @Transactional
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
        userFromDb.setPendingPhoneNumber(newNumber);
        applicationEventPublisher.publishEvent(new PendingPhoneNumberCreatedEvent(userFromDb));
    }

    @Override
    @Transactional
    public void updateUserEmail(Long id, UpdateUserEmailRequest requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, requestDto.token());
        userFromDb.setEmail(userFromDb.getPendingEmail());
        userFromDb.setPendingEmail(null);
    }

    @Override
    public void send2faUpdateSms(Long id) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiException("Phone number is not verified", "phoneNumber");
        }
        if (userFromDb.isTwoFactorAuth()) {
            throw new ApiException("Phone number already has 2fa enabled", "phoneNumber");
        }
        applicationEventPublisher.publishEvent(new MfaUpdateRequestedEvent(userFromDb));
    }

    @Override
    @Transactional
    public void updateUser2fa(Long id, UpdateUser2FARequest request) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        tokenService.validateAndDeleteToken(userFromDb, request.token());
        userFromDb.setTwoFactorAuth(true);
    }
}
