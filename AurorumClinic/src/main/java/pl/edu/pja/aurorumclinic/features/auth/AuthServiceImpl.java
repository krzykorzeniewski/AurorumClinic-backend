package pl.edu.pja.aurorumclinic.features.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.TokenUtils;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.features.auth.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.auth.dtos.response.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final SmsService smsService;
    private final TokenUtils tokenUtils;

    @Value("${mail.frontend.verification-link}")
    private String mailVerificationLink;

    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Override
    public User registerEmployee(RegisterEmployeeRequest requestDto) {
        if (userRepository.findByEmail(requestDto.email()) != null) {
            throw new ApiException("Email already in use", "email");
        }
        User employee = User.builder()
                .name(requestDto.name())
                .surname(requestDto.surname())
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(UserRole.EMPLOYEE)
                .birthdate(requestDto.birthDate())
                .pesel(requestDto.pesel())
                .phoneNumber(requestDto.phoneNumber())
                .build();
        userRepository.save(employee);
        sendVerificationEmail(employee);
        return employee;
    }

    @Override
    public Patient registerPatient(RegisterPatientRequest requestDto) {
        if (userRepository.findByEmail(requestDto.email()) != null) {
            throw new ApiException("Email already in use", "email");
        }
        Patient patient = Patient.builder()
                .name(requestDto.name())
                .surname(requestDto.surname())
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(UserRole.PATIENT)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .birthdate(requestDto.birthDate())
                .pesel(requestDto.pesel())
                .phoneNumber(requestDto.phoneNumber())
                .build();
        userRepository.save(patient);
        sendVerificationEmail(patient);
        return patient;
    }

    @Override
    public Doctor registerDoctor(RegisterDoctorRequest requestDto) {
        if (userRepository.findByEmail(requestDto.email()) != null) {
            throw new ApiException("Email already in use", "email");
        }
        Doctor doctor = Doctor.builder()
                .name(requestDto.name())
                .surname(requestDto.surname())
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(UserRole.DOCTOR)
                .birthdate(requestDto.birthDate())
                .pesel(requestDto.pesel())
                .phoneNumber(requestDto.phoneNumber())
                .description(requestDto.description())
                .specialization(requestDto.specialization())
                .education(requestDto.education())
                .experience(requestDto.experience())
                .pwzNumber(requestDto.pwzNumber())
                .build();
        userRepository.save(doctor);
        sendVerificationEmail(doctor);
        return doctor;
    }

    @Override
    public LoginUserResponse loginUser(LoginUserRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Invalid credentials", "credentials");
        }
        if (!passwordEncoder.matches(requestDto.password(), userFromDb.getPassword())) {
            throw new ApiAuthException("Invalid credentials", "credentials");
        }
        if (!userFromDb.isEmailVerified()) {
            throw new ApiAuthException("Email is not verified", "email");
        }

        if (userFromDb.isTwoFactorAuth()) {
            send2faLoginSms(userFromDb);
            return LoginUserResponse.builder()
                    .userId(userFromDb.getId())
                    .accessToken(null)
                    .refreshToken(null)
                    .email(userFromDb.getEmail())
                    .twoFactorAuth(userFromDb.isTwoFactorAuth())
                    .role(userFromDb.getRole())
                    .build();
        }

        String jwt = jwtUtils.createJwt(userFromDb);
        String refreshToken = tokenUtils.createRandomToken();

        userFromDb.setRefreshToken(passwordEncoder.encode(refreshToken));
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));

        return LoginUserResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();
    }

    @Override
    public RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest requestDto) {
        String jwt = requestDto.accessToken();
        Long userId;
        try {
            jwtUtils.validateJwt(jwt);
            userId = jwtUtils.getUserIdFromJwt(jwt);
        } catch (ExpiredJwtException e) {
            userId = jwtUtils.getUserIdFromExpiredJwt(jwt);
        } catch (JwtException jwtException) {
            throw new ApiAuthException(jwtException.getMessage(), "accessToken");
        }

        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiAuthException("Invalid credentials", "credentials")
        );

        if (!passwordEncoder.matches(requestDto.refreshToken(), userFromDb.getRefreshToken())) {
            throw new ApiAuthException("Invalid credentials", "credentials");
        }

        if (userFromDb.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Refresh token is expired", "refreshToken");
        }

        String newJwt = jwtUtils.createJwt(userFromDb);
        String newRefreshToken = tokenUtils.createRandomToken();

        userFromDb.setRefreshToken(passwordEncoder.encode(newRefreshToken));
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));

        return RefreshAccessTokenResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();
    }

    @Override
    public void verifyUserEmail(VerifyEmailRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (!passwordEncoder.matches(requestDto.token(), userFromDb.getEmailVerificationToken())) {
            throw new ApiAuthException("Invalid token", "token");
        }
        if (userFromDb.getEmailVerificationExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Verification token is expired", "token");
        }
        userFromDb.setEmailVerified(true);
        userFromDb.setEmailVerificationToken(null);
        userFromDb.setEmailVerificationExpiryDate(null);
    }

    @Override
    public void sendResetPasswordEmail(PasswordResetTokenRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            return;
        }
        if (!userFromDb.isEmailVerified()) {
            return;
        }

        String token = tokenUtils.createRandomToken();
        userFromDb.setPasswordResetToken(passwordEncoder.encode(token));
        userFromDb.setPasswordResetExpiryDate(LocalDateTime.now().plusMinutes(15));

        String verificationLink = resetPasswordLink + token;
        emailService.sendEmail(
                "support@aurorumclinic.pl",
                userFromDb.getEmail(),
                "Ustaw nowe hasło",
                "Naciśnij link aby zresetować hasło: " + verificationLink
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (!passwordEncoder.matches(requestDto.token(), userFromDb.getPasswordResetToken())) {
            throw new ApiAuthException("Invalid token", "token");
        }
        if (userFromDb.getPasswordResetExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Token is expired", "token");
        }
        userFromDb.setPassword(passwordEncoder.encode(requestDto.password()));
        userFromDb.setPasswordResetToken(null);
        userFromDb.setPasswordResetExpiryDate(null);
    }

    @Override
    public TwoFactorAuthLoginResponse loginUserWithTwoFactorAuth(TwoFactorAuthLoginRequest requestDto) {
        User userFromDb = userRepository.findById(requestDto.userId()).orElseThrow(
                () -> new ApiAuthException("User not found", "id")
        );
        if (!passwordEncoder.matches(requestDto.otp(), userFromDb.getTwoFactorAuthToken())) {
            throw new ApiAuthException("Code is invalid", "otp");
        }
        if (userFromDb.getTwoFactorAuthExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Code is expired", "otp");
        }
        userFromDb.setTwoFactorAuthToken(null);
        userFromDb.setTwoFactorAuthExpiryDate(null);
        String newJwt = jwtUtils.createJwt(userFromDb);
        String newRefreshToken = tokenUtils.createRandomToken();

        userFromDb.setRefreshToken(passwordEncoder.encode(newRefreshToken));
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));

        return TwoFactorAuthLoginResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();
    }

    @Override
    public void sendVerifyUserAccountEmail(VerifyEmailTokenRequest verifyEmailTokenRequest) {
        User userFromDb = userRepository.findByEmail(verifyEmailTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        sendVerificationEmail(userFromDb);
    }

    @Override
    public void send2faToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        User userFromDb = userRepository.findByEmail(twoFactorAuthTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiAuthException("Phone number is not verified", "phoneNumber");
        }
        if (!userFromDb.isTwoFactorAuth()) {
            throw new ApiAuthException("Given email has 2fa disabled", "email");
        }
        send2faLoginSms(userFromDb);
    }

    @Override
    public void sendVerifyPhoneNumberMessage(VerifyPhoneNumberTokenRequest requestDto, Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(requestDto.phoneNumber());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new AuthorizationDeniedException("Access denied");
        }
        sendPhoneNumberVerificationSms(userFromDb);
    }

    @Override
    public void verifyPhoneNumber(VerifyPhoneNumberRequest requestDto, Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(requestDto.phoneNumber());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new AuthorizationDeniedException("Access denied");
        }
        if (!passwordEncoder.matches(requestDto.token(), userFromDb.getPhoneNumberVerificationToken())) {
            throw new ApiAuthException("Invalid token", "token");
        }
        if (userFromDb.getPhoneNumberVerificationExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Otp is expired", "token");
        }
        userFromDb.setPhoneNumberVerificationToken(null);
        userFromDb.setPhoneNumberVerificationExpiryDate(null);
        userFromDb.setPhoneNumberVerified(true);
    }

    private void sendVerificationEmail(User user) {
        String token = tokenUtils.createRandomToken();
        user.setEmailVerificationToken(passwordEncoder.encode(token));
        user.setEmailVerificationExpiryDate(LocalDateTime.now().plusMinutes(15));

        String verificationLink = mailVerificationLink + token;

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                user.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
    }

    private void send2faLoginSms(User user) {
        String otp = tokenUtils.createOtp();
        user.setTwoFactorAuthToken(passwordEncoder.encode(otp));
        user.setTwoFactorAuthExpiryDate(LocalDateTime.now().plusMinutes(5));

        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + otp);
    }

    private void sendPhoneNumberVerificationSms(User user) {
        String otp = tokenUtils.createOtp();
        user.setPhoneNumberVerificationToken(passwordEncoder.encode(otp));
        user.setPhoneNumberVerificationExpiryDate(LocalDateTime.now().plusMinutes(5));

        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic : " + otp);
    }

}
