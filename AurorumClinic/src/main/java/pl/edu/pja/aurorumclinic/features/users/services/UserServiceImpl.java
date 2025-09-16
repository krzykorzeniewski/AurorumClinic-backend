package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;
import pl.edu.pja.aurorumclinic.shared.TokenUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenUtils tokenUtils;
    private final SmsService smsService;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Value("${mail.backend.noreply-address}")
    private String fromEmailAddress;

    @Override
    public void updateUserEmail(Long id, UpdateUserEmailRequest requestDto, Authentication authentication) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(authentication.getPrincipal(), userFromDb.getEmail())) {
            throw new ApiException("Id does not correspond to the user's id", "id");
        }
    }

    @Override
    public void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto, Authentication authentication) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(authentication.getPrincipal(), userFromDb.getEmail())) {
            throw new ApiException("Id does not correspond to the user's id", "id");
        }
    }
}
