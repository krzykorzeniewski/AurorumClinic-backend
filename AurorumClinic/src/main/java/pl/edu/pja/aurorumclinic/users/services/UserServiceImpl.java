package pl.edu.pja.aurorumclinic.users.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.User;
import pl.edu.pja.aurorumclinic.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.security.SecurityUtils;
import pl.edu.pja.aurorumclinic.security.exceptions.ExpiredRefreshTokenException;
import pl.edu.pja.aurorumclinic.security.exceptions.InvalidAccessTokenException;
import pl.edu.pja.aurorumclinic.security.exceptions.RefreshTokenNotFoundException;
import pl.edu.pja.aurorumclinic.shared.EmailService;
import pl.edu.pja.aurorumclinic.shared.SmsService;
import pl.edu.pja.aurorumclinic.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.users.dtos.response.LoginUserResponse;
import pl.edu.pja.aurorumclinic.users.dtos.response.RefreshAccessTokenResponse;
import pl.edu.pja.aurorumclinic.users.dtos.response.TwoFactorAuthLoginResponse;
import pl.edu.pja.aurorumclinic.users.shared.EmailNotUniqueException;
import pl.edu.pja.aurorumclinic.users.UserRepository;
import pl.edu.pja.aurorumclinic.users.shared.EmailVerificationTokenNotFoundException;
import pl.edu.pja.aurorumclinic.users.shared.ResourceNotFoundException;
import pl.edu.pja.aurorumclinic.users.shared.UserEmailNotVerifiedException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final AuthenticationProvider authenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${mail.verification-link}")
    private String mailVerificationLink;

    @Value("${mail.frontend.password-reset-link}")
    private String resetPasswordLink;

    @Value("${twilio.trial_number}")
    private String fromPhoneNumber;

    @Override
    public User registerEmployee(RegisterEmployeeRequest requestDto) {
        if (userRepository.findByEmail(requestDto.email()) != null) {
            throw new EmailNotUniqueException("Email already in use:" + requestDto.email());
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
            throw new EmailNotUniqueException("Email already in use:" + requestDto.email());
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
            throw new EmailNotUniqueException("Email already in use:" + requestDto.email());
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
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                requestDto.email(), requestDto.password()
        ));

        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (!userFromDb.isEmailVerified()) {
            throw new UserEmailNotVerifiedException("Account is not verified");
        }

        if (userFromDb.isTwoFactorAuth()) {
            sendOtpSms(userFromDb);
            return LoginUserResponse.builder()
                    .userId(userFromDb.getId())
                    .accessToken(null)
                    .refreshToken(null)
                    .twoFactorAuth(userFromDb.isTwoFactorAuth())
                    .build();
        }

        String jwt = securityUtils.createJwt(userFromDb);
        String refreshToken = securityUtils.createRefreshToken();

        userFromDb.setRefreshToken(refreshToken);
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(userFromDb);

        return LoginUserResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .build();
    }

    @Override
    public RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest requestDto) {
        String jwt = requestDto.accessToken();
        try {
            securityUtils.validateJwt(jwt);
        } catch (JwtException jwtException) {
            if (jwtException instanceof ExpiredJwtException) {
                System.out.println("token expired");
            } else {
                throw new InvalidAccessTokenException(jwtException.getMessage());
            }
        }

        User userFromDb = userRepository.findByRefreshToken(requestDto.refreshToken());
        if (userFromDb == null) {
            throw new RefreshTokenNotFoundException("Invalid refresh token");
        }

        if (userFromDb.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Refresh token is expired");
        }

        String newJwt = securityUtils.createJwt(userFromDb);
        String newRefreshToken = securityUtils.createRefreshToken();

        userFromDb.setRefreshToken(newRefreshToken);
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(userFromDb);

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
            throw new EmailVerificationTokenNotFoundException("Invalid verification token");
        }
        if (userFromDb.getEmailVerificationExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Verification token is expired");
        }
        userFromDb.setEmailVerified(true);
        userFromDb.setEmailVerificationToken(null);
        userFromDb.setEmailVerificationExpiryDate(null);
        userRepository.save(userFromDb);
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

        String token = securityUtils.createRandomToken();
        userFromDb.setPasswordResetToken(token);
        userFromDb.setPasswordResetExpiryDate(LocalDateTime.now().plusMinutes(15));
        userRepository.save(userFromDb);

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
            throw new ResourceNotFoundException("Invalid password reset token");
        }
        if (userFromDb.getPasswordResetExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Reset password token is expired");
        }
        userFromDb.setPassword(passwordEncoder.encode(requestDto.password()));
        userFromDb.setPasswordResetToken(null);
        userFromDb.setPasswordResetExpiryDate(null);
        userRepository.save(userFromDb);
    }

    @Override
    public TwoFactorAuthLoginResponse loginUserWithTwoFactorAuth(TwoFactorAuthLoginRequest requestDto) {
        User userFromDb = userRepository.findByTwoFactorAuthToken(requestDto.code());
        if (userFromDb == null) {
            throw new BadCredentialsException("Invalid 2fa token");
        }
        if (userFromDb.getTwoFactorAuthExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Expired 2fa token");
        }
        userFromDb.setTwoFactorAuthToken(null);
        userFromDb.setTwoFactorAuthExpiryDate(null);
        String newJwt = securityUtils.createJwt(userFromDb);
        String newRefreshToken = securityUtils.createRefreshToken();

        userFromDb.setRefreshToken(newRefreshToken);
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(userFromDb);

        return TwoFactorAuthLoginResponse.builder()
                .userId(userFromDb.getId())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .build();
    }

    @Override
    public void sendVerifyUserAccountEmail(VerifyEmailTokenRequest verifyEmailTokenRequest) {
        User userFromDb = userRepository.findByEmail(verifyEmailTokenRequest.email());
        if (userFromDb == null) {
            throw new ResourceNotFoundException("User with email does not exist:" + verifyEmailTokenRequest.email());
        }
        sendVerificationEmail(userFromDb);
    }

    @Override
    public void send2faToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        User userFromDb = userRepository.findByEmail(twoFactorAuthTokenRequest.email());
        if (userFromDb == null) {
            throw new ResourceNotFoundException("User with email does not exist:" + twoFactorAuthTokenRequest.email());
        }
        if (!userFromDb.isTwoFactorAuth()) {
            throw new IllegalArgumentException("User has 2fa disabled: " + twoFactorAuthTokenRequest.email());
        }
        sendOtpSms(userFromDb);
    }

    private void sendVerificationEmail(User user) {
        String token = securityUtils.createRandomToken();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiryDate(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String verificationLink = mailVerificationLink + token;

        emailService.sendEmail(
                "support@aurorumclinic.pl",
                user.getEmail(),
                "Weryfikacja konta",
                "Naciśnij link aby zweryfikować adres email: " + verificationLink
        );
    }

    private void sendOtpSms(User user) {
        String otp = securityUtils.createOtp();
        user.setTwoFactorAuthToken(otp);
        user.setTwoFactorAuthExpiryDate(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        smsService.sendSms("+48"+user.getPhoneNumber(), fromPhoneNumber,
                "Kod logowania do Aurorum Clinic : " + otp);
    }
}
