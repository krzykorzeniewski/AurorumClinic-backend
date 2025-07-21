package pl.edu.pja.aurorumclinic.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.security.SecurityUtils;
import pl.edu.pja.aurorumclinic.security.exceptions.ExpiredRefreshTokenException;
import pl.edu.pja.aurorumclinic.security.exceptions.RefreshTokenNotFoundException;
import pl.edu.pja.aurorumclinic.users.UserRepository;
import pl.edu.pja.aurorumclinic.users.dtos.LoginPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RefreshTokenRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RegisterPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.TokenResponseDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final UserRepository userRepository;
    private final AuthenticationProvider authenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public void registerPatient(RegisterPatientRequestDto requestDto) throws Exception {
        if (userRepository.findByEmail(requestDto.email()) != null) {
            throw new Exception("Email already in use:" + requestDto.email());
        }
        Patient patient = Patient.builder()
                .name(requestDto.name())
                .surname(requestDto.surname())
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(UserRole.PATIENT)
                .birthdate(requestDto.birthDate())
                .pesel(requestDto.pesel())
                .phoneNumber(requestDto.phoneNumber())
                .build();
        userRepository.save(patient);
    }

    @Override
    public TokenResponseDto loginPatient(LoginPatientRequestDto requestDto) {
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                requestDto.email(), requestDto.password()
        ));

        Patient patientFromDb = (Patient) userRepository.findByEmail(requestDto.email());
        if (patientFromDb == null) {
            throw new UsernameNotFoundException("Email not found: " + requestDto.email());
        }

        String jwt = securityUtils.createJwt(patientFromDb);
        String refreshToken = securityUtils.createRefreshToken();

        patientFromDb.setRefreshToken(refreshToken);
        patientFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(patientFromDb);

        return TokenResponseDto.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponseDto refreshAccessToken(RefreshTokenRequestDto requestDto) {
        Patient patientFromDb = (Patient) userRepository.findByRefreshToken(requestDto.refreshToken());
        if (patientFromDb == null) {
            throw new RefreshTokenNotFoundException("Invalid refresh token!");
        }

        if (patientFromDb.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredRefreshTokenException("Refresh token is expired!");
        }

        String newJwt = securityUtils.createJwt(patientFromDb);
        String newRefreshToken = securityUtils.createRefreshToken();

        patientFromDb.setRefreshToken(newRefreshToken);
        patientFromDb.setRefreshTokenExpiryDate(LocalDateTime.now().plusDays(1));
        userRepository.save(patientFromDb);

        return TokenResponseDto.builder()
                .accessToken(newJwt)
                .refreshToken(newRefreshToken)
                .build();
    }
}
