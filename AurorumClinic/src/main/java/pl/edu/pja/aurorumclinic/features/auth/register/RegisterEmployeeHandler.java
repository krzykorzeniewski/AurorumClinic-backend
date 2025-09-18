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
import pl.edu.pja.aurorumclinic.features.auth.register.events.UserRegisteredEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth/register-employee")
@RequiredArgsConstructor
public class RegisterEmployeeHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping
    @Transactional
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest requestDto) {
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
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(employee));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    public record RegisterEmployeeRequest(@NotBlank @Size(max = 50) String name,
                                          @NotBlank @Size(max = 50) String surname,
                                          @Size(min = 11, max = 11) String pesel,
                                          @NotNull LocalDate birthDate,
                                          @NotBlank @Email @Size(max = 100) String email,
                                          @NotBlank @Size(max = 200) String password,
                                          @NotBlank @Size(max = 9) String phoneNumber) {
    }

}
