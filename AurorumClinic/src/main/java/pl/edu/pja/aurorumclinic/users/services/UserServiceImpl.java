package pl.edu.pja.aurorumclinic.users.services;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
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
import pl.edu.pja.aurorumclinic.users.shared.EmailNotUniqueException;
import pl.edu.pja.aurorumclinic.users.UserRepository;
import pl.edu.pja.aurorumclinic.users.dtos.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final AuthenticationProvider authenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public User registerEmployee(RegisterEmployeeRequestDto requestDto) {
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
        return employee;
    }

    @Override
    public Patient registerPatient(RegisterPatientRequestDto requestDto) {
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
        return patient;
    }

    @Override
    public Doctor registerDoctor(RegisterDoctorRequestDto requestDto) {
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
        return doctor;
    }

    @Override
    public AccessTokenDto loginUser(LoginUserRequestDto requestDto) {
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                requestDto.email(), requestDto.password()
        ));

        User userFromDb = userRepository.findByEmail(requestDto.email());
        String jwt = securityUtils.createJwt(userFromDb);
        String refreshToken = securityUtils.createRefreshToken();

        userFromDb.setRefreshToken(refreshToken);
        userFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(userFromDb);

        return AccessTokenDto.builder()
                .userId(userFromDb.getId())
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AccessTokenDto refreshAccessToken(RefreshTokenRequestDto requestDto) {
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

        return AccessTokenDto.builder()
                .userId(userFromDb.getId())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .build();
    }

}
