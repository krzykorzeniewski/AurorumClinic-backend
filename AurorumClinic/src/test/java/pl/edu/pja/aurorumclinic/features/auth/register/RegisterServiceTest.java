package pl.edu.pja.aurorumclinic.features.auth.register;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.auth.register.dtos.*;
import pl.edu.pja.aurorumclinic.features.auth.register.events.*;
import pl.edu.pja.aurorumclinic.shared.PasswordValidator;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {RegisterServiceImpl.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class RegisterServiceTest {

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    SpecializationRepository specializationRepository;

    @MockitoBean
    PasswordValidator passwordValidator;

    @Value("${email-verification-token.expiration.minutes}")
    private Integer emailVerificationTokenExpirationInMinutes;

    @Autowired
    RegisterServiceImpl registerService;

    @Test
    void registerDoctorShouldThrowApiExceptionWhenSpecializationIdsAreNotAllFound() {
        RegisterDoctorRequest request = new RegisterDoctorRequest(
                "Mariusz",
                "Kowalski",
                "11111111111",
                LocalDate.now().minusYears(30),
                "mariusz@example.com",
                "123123123",
                "Psychiatra dorosłych specjalizujący się w leczeniu uzależnień",
                "Pomorski Uniwersytet Medyczny w Szczecinie",
                "Specjalizacja na oddziale psychiatrii w słupskim szpitalu wojewódzkim",
                "PWZ12312235",
                Set.of(1L, 2L, 3L)
        );

        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(new Specialization()));

        assertThatThrownBy(() -> registerService.registerDoctor(request)).isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void registerDoctorShouldSaveDoctorEntityAndPublishEventWhenRequestDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents) {
        Specialization testSpecialization = Specialization.builder()
                .id(1L)
                .build();
        RegisterDoctorRequest request = new RegisterDoctorRequest(
                "Mariusz",
                "Kowalski",
                "11111111111",
                LocalDate.now().minusYears(30),
                "mariusz@example.com",
                "123123123",
                "Psychiatra dorosłych specjalizujący się w leczeniu uzależnień",
                "Pomorski Uniwersytet Medyczny w Szczecinie",
                "Specjalizacja na oddziale psychiatrii w słupskim szpitalu wojewódzkim",
                "PWZ12312235",
                Set.of(testSpecialization.getId())
        );
        String randomPassword = "Haslo123456789";

        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(testSpecialization));
        when(tokenService.createRandomToken()).thenReturn(randomPassword);
        when(passwordEncoder.encode(anyString())).then(invocation -> invocation.getArgument(0));

        registerService.registerDoctor(request);

        ArgumentCaptor<Doctor> doctorArgumentCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(userRepository).save(doctorArgumentCaptor.capture());
        Doctor savedDoctor = doctorArgumentCaptor.getValue();

        assertThat(savedDoctor).isNotNull();
        assertThat(savedDoctor.getName()).isEqualTo(request.name());
        assertThat(savedDoctor.getSurname()).isEqualTo(request.surname());
        assertThat(savedDoctor.getEmail()).isEqualTo(request.email());
        assertThat(savedDoctor.getPassword()).isEqualTo(randomPassword.substring(0, 10));
        assertThat(savedDoctor.getRole()).isEqualTo(UserRole.DOCTOR);
        assertThat(savedDoctor.getBirthdate()).isEqualTo(request.birthDate());
        assertThat(savedDoctor.getPesel()).isEqualTo(request.pesel());
        assertThat(savedDoctor.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(savedDoctor.getDescription()).isEqualTo(request.description());
        assertThat(savedDoctor.getEducation()).isEqualTo(request.education());
        assertThat(savedDoctor.getExperience()).isEqualTo(request.experience());
        assertThat(savedDoctor.getPwzNumber()).isEqualTo(request.pwzNumber());
        assertThat(savedDoctor.isEmailVerified()).isEqualTo(true);
        assertThat(savedDoctor.getSpecializations()).containsExactly(testSpecialization);

        assertThat(applicationEvents.stream(DoctorRegisteredEvent.class))
                .filteredOn(
                        event ->
                                Objects.equals(event.doctor(), savedDoctor) &&
                                        Objects.equals(event.password(), randomPassword.substring(0, 10)))
                .hasSize(1);
    }

    @Test
    void registerPatientShouldSavePatientEntityAndPublishEventWhenRequestDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
    ) {
        RegisterPatientRequest request = new RegisterPatientRequest(
                "Mariusz",
                "Kowalski",
                "11111111111",
                LocalDate.now().minusYears(30),
                "mariusz@example.com",
                "Haslo12345",
                "123123123"
        );
        Token emailVerificationToken = Token.builder()
                .value("some hashed value")
                .rawValue("some raw value")
                .build();

        passwordValidator.validatePassword(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn(request.password());
        when(tokenService.createToken(any(User.class), any(TokenName.class), anyInt()))
                .thenReturn(emailVerificationToken);

        registerService.registerPatient(request);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(userRepository).save(patientArgumentCaptor.capture());

        Patient savedPatient = patientArgumentCaptor.getValue();
        verify(tokenService).createToken(savedPatient, TokenName.EMAIL_VERIFICATION,
                emailVerificationTokenExpirationInMinutes);

        assertThat(savedPatient).isNotNull();
        assertThat(savedPatient.getName()).isEqualTo(request.name());
        assertThat(savedPatient.getSurname()).isEqualTo(request.surname());
        assertThat(savedPatient.getEmail()).isEqualTo(request.email());
        assertThat(savedPatient.getPassword()).isEqualTo(request.password());
        assertThat(savedPatient.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(savedPatient.getCommunicationPreferences()).isEqualTo(CommunicationPreference.EMAIL);
        assertThat(savedPatient.getPesel()).isEqualTo(request.pesel());
        assertThat(savedPatient.getBirthdate()).isEqualTo(request.birthDate());
        assertThat(savedPatient.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(savedPatient.isEmailVerified()).isEqualTo(false);

        assertThat(applicationEvents.stream(PatientRegisteredEvent.class))
                .filteredOn(
                        event ->
                                Objects.equals(event.user(), savedPatient) &&
                                        Objects.equals(event.token(), emailVerificationToken))
                .hasSize(1);
    }

    @Test
    void registerEmployeeShouldSaveUserEntityAndPublishEventWhenRequestDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents) {
        RegisterEmployeeRequest request = new RegisterEmployeeRequest(
                "Mariusz",
                "Kowalski",
                "11111111111",
                LocalDate.now().minusYears(30),
                "mariusz@example.com",
                "123123123"
        );
        String randomPassword = "totallyRandomPassword";
        String correctPassword = randomPassword.substring(0, 10);
        when(tokenService.createRandomToken()).thenReturn(randomPassword);
        when(passwordEncoder.encode(anyString())).then(invocation -> invocation.getArgument(0));

        registerService.registerEmployee(request);

        ArgumentCaptor<User> employeeArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(employeeArgumentCaptor.capture());
        verify(passwordEncoder).encode(correctPassword);

        User savedEmployee = employeeArgumentCaptor.getValue();
        assertThat(savedEmployee).isNotNull();
        assertThat(savedEmployee.getName()).isEqualTo(request.name());
        assertThat(savedEmployee.getSurname()).isEqualTo(request.surname());
        assertThat(savedEmployee.getEmail()).isEqualTo(request.email());
        assertThat(savedEmployee.getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(savedEmployee.getPassword()).isEqualTo(correctPassword);
        assertThat(savedEmployee.getPesel()).isEqualTo(request.pesel());
        assertThat(savedEmployee.getBirthdate()).isEqualTo(request.birthDate());
        assertThat(savedEmployee.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(savedEmployee.isEmailVerified()).isEqualTo(true);

        assertThat(applicationEvents.stream(EmployeeRegisteredEvent.class))
                .filteredOn(
                        event ->
                                Objects.equals(event.user(), savedEmployee) &&
                                        Objects.equals(event.password(), correctPassword))
                .hasSize(1);
    }

    @Test
    void createVerifyEmailTokenShouldThrowApiNotFoundExceptionWhenEmailIsNotFound() {
        VerifyEmailTokenRequest request = new VerifyEmailTokenRequest("mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatThrownBy(() -> registerService.createVerifyEmailToken(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void createVerifyEmailTokenShouldThrowApiExceptionWhenEmailIsVerified() {
        VerifyEmailTokenRequest request = new VerifyEmailTokenRequest("mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(User.builder()
                        .emailVerified(true)
                .build());

        assertThatThrownBy(() -> registerService.createVerifyEmailToken(request))
                .isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void createVerifyEmailTokenShouldCreateEmailVerificationTokenAndPublishEventWhenDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
    ) {
        VerifyEmailTokenRequest request = new VerifyEmailTokenRequest("mariusz@example.com");
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(false)
                .build();
        Token testEmailVerificationToken = Token.builder()
                .name(TokenName.EMAIL_VERIFICATION)
                .value("some hashed value")
                .rawValue("some raw value")
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        when(tokenService.createToken(any(User.class), any(TokenName.class), anyInt()))
                .thenReturn(testEmailVerificationToken);

        registerService.createVerifyEmailToken(request);

        verify(tokenService).createToken(testUser, TokenName.EMAIL_VERIFICATION,
                emailVerificationTokenExpirationInMinutes);

        assertThat(applicationEvents.stream(EmailVerificationTokenCreatedEvent.class))
                .filteredOn(event -> Objects.equals(event.user(), testUser) &&
                        Objects.equals(event.token(), testEmailVerificationToken))
                .hasSize(1);

    }

    @Test
    void verifyEmailShouldThrowApiNotFoundExceptionWhenEmailIsNotFound() {
        VerifyEmailRequest request = new VerifyEmailRequest("random token", "mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(null);

        assertThatThrownBy(() -> registerService.verifyEmail(request)).isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void verifyEmailShouldThrowApiExceptionWhenEmailIsVerified() {
        VerifyEmailRequest request = new VerifyEmailRequest("random token", "mariusz@example.com");

        when(userRepository.findByEmail(request.email())).thenReturn(User.builder()
                .emailVerified(true)
                .build());

        assertThatThrownBy(() -> registerService.verifyEmail(request)).isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void verifyEmailShouldSetEmailVerifiedTrueWhenDataIsCorrect() {
        VerifyEmailRequest request = new VerifyEmailRequest("random token", "mariusz@example.com");
        User testUser = User.builder()
                .email("mariusz@example.com")
                .password("1234")
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(testUser);
        tokenService.validateAndDeleteToken(any(User.class), anyString());

        registerService.verifyEmail(request);

        verify(tokenService).validateAndDeleteToken(testUser, request.token());
        assertThat(testUser.isEmailVerified()).isEqualTo(true);
    }

    @Test
    void createNewPasswordShouldThrowApiNotFoundExceptionWhenIdIsNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> registerService.createNewPassword(1L))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void createNewPasswordShouldThrowApiExceptionWhenUserHasPatientRole() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(
                User.builder()
                        .role(UserRole.PATIENT)
                        .build()
        ));
        assertThatThrownBy(() -> registerService.createNewPassword(1L))
                .isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void createNewPasswordShouldSetNewPasswordAndEventWhenDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
    ) {
        User testUser = User.builder()
                .id(1L)
                .password("1234")
                .role(UserRole.DOCTOR)
                .build();
        String randomPassword = "totallyRandomPassword";
        String correctPassword = randomPassword.substring(0, 10);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(
                testUser
        ));
        when(tokenService.createRandomToken()).thenReturn(randomPassword);
        when(passwordEncoder.encode(anyString())).then(invocation -> invocation.getArgument(0));

        registerService.createNewPassword(testUser.getId());

        assertThat(testUser.getPassword()).isEqualTo(correctPassword);
        assertThat(applicationEvents.stream(StaffMemberPasswordCreatedEvent.class))
                .filteredOn(event -> Objects.equals(event.user(), testUser)
                        && Objects.equals(event.password(), correctPassword))
                .hasSize(1);
    }

}
