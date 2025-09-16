package pl.edu.pja.aurorumclinic.features.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.dtos.response.GetBasicUserInfoResponse;
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

    @Value("${mail.verification-link}")
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
            send2faSms(userFromDb);
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

        userFromDb.setRefreshToken(refreshToken);
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
        try {
            jwtUtils.validateJwt(jwt);
        } catch (JwtException jwtException) {
            if (jwtException instanceof ExpiredJwtException) {
                System.out.println("token expired");
            } else {
                throw new ApiAuthException(jwtException.getMessage(), "accessToken");
            }
        }

        User userFromDb = userRepository.findByRefreshToken(requestDto.refreshToken());
        if (userFromDb == null) {
            throw new ApiAuthException("Refresh token is invalid", "refreshToken");
        }

        if (userFromDb.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Refresh token is expired", "refreshToken");
        }

        String newJwt = jwtUtils.createJwt(userFromDb);
        String newRefreshToken = tokenUtils.createRandomToken();

        userFromDb.setRefreshToken(newRefreshToken);
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));

        return RefreshAccessTokenResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void verifyUserEmail(String token) {
        User userFromDb = userRepository.findByEmailVerificationToken(token);
        if (userFromDb == null) {
            throw new ApiAuthException("Verification token is invalid", "token");
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
        userFromDb.setPasswordResetToken(token);
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
        User userFromDb = userRepository.findByPasswordResetToken(requestDto.token());
        if (userFromDb == null) {
            throw new ApiAuthException("Token is invalid", "token");
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
        User userFromDb = userRepository.findByTwoFactorAuthToken(requestDto.code());
        if (userFromDb == null) {
            throw new ApiAuthException("Code is invalid", "code");
        }
        if (userFromDb.getTwoFactorAuthExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Code is expired", "code");
        }
        userFromDb.setTwoFactorAuthToken(null);
        userFromDb.setTwoFactorAuthExpiryDate(null);
        String newJwt = jwtUtils.createJwt(userFromDb);
        String newRefreshToken = tokenUtils.createRandomToken();

        userFromDb.setRefreshToken(newRefreshToken);
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
        send2faSms(userFromDb);
    }

    @Override
    public GetBasicUserInfoResponse getBasicUserInfo(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User userFromDb = userRepository.findByEmail(email);
        if (userFromDb == null) {
            throw new ApiAuthException("Email not found", "email");
        }
        return GetBasicUserInfoResponse.builder()
                .userId(userFromDb.getId())
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();
    }

    @Override
    public void sendVerifyPhoneNumberMessage(VerifyPhoneNumberTokenRequest requestDto) {
        User userFromDb = userRepository.findByPhoneNumber(requestDto.phoneNumber());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number not found", "phoneNumber");
        }
        sendOtpSms(userFromDb);
    }

    @Override
    public void verifyPhoneNumber(VerifyPhoneNumberRequest requestDto) {
        User userFromDb = userRepository.findByPhoneNumberVerificationToken(requestDto.phoneNumberVerificationToken());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number verification token is invalid", "phoneNumberVerificationToken");
        }
        if (userFromDb.getPhoneNumberVerificationExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Otp is expired", "phoneNumberVerificationToken");
        }
        userFromDb.setPhoneNumberVerificationToken(null);
        userFromDb.setPhoneNumberVerificationExpiryDate(null);
        userFromDb.setPhoneNumberVerified(true);
    }

    private void sendVerificationEmail(User user) {
        String token = tokenUtils.createRandomToken();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiryDate(LocalDateTime.now().plusMinutes(15));

        String verificationLink = mailVerificationLink + token;

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                user.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
    }

    private void send2faSms(User user) {
        String otp = tokenUtils.createOtp();
        user.setTwoFactorAuthToken(otp);
        user.setTwoFactorAuthExpiryDate(LocalDateTime.now().plusMinutes(5));

        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + otp);
    }

    private void sendOtpSms(User user) {
        String otp = tokenUtils.createOtp();
        user.setPhoneNumberVerificationToken(otp);
        user.setPhoneNumberVerificationExpiryDate(LocalDateTime.now().plusMinutes(5));

        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod weryfikacyjny numeru telefonu w Aurorum Clinic : " + otp);
    }

}
