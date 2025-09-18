package pl.edu.pja.aurorumclinic.features.auth.register;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth/register-patient")
@RequiredArgsConstructor
public class RegisterPatientHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping
    @Transactional
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterPatientRequest requestDto) {
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
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(patient));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    public record RegisterPatientRequest(@NotBlank @Size(max = 50) String name,
                                         @NotBlank @Size(max = 50) String surname,
                                         @Size(min = 11, max = 11) String pesel,
                                         @NotNull LocalDate birthDate,
                                         @NotBlank @Email @Size(max = 100) String email,
                                         @NotBlank @Size(max = 200) String password,
                                         @NotBlank @Size(max = 9) String phoneNumber) {
    }
}
