package pl.edu.pja.aurorumclinic.features.auth.register;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SpecializationRepository specializationRepository;
    private final PasswordValidator passwordValidator;

    @Value("${email-verification-token.expiration.minutes}")
    private Integer emailVerificationTokenExpirationInMinutes;

    @Override
    @Transactional
    public void registerDoctor(RegisterDoctorRequest registerDoctorRequest) {
        List<Specialization> specializationsFromDb = specializationRepository.findAllById(
                registerDoctorRequest.specializationIds());
        if (specializationsFromDb.size() != registerDoctorRequest.specializationIds().size()) {
            throw new ApiException("Some specialization ids are not found", "specializationIds");
        }
        String randomPassword = tokenService.createRandomToken().substring(0, 10);
        Doctor doctor = Doctor.builder()
                .name(registerDoctorRequest.name())
                .surname(registerDoctorRequest.surname())
                .email(registerDoctorRequest.email())
                .password(passwordEncoder.encode(randomPassword))
                .role(UserRole.DOCTOR)
                .birthdate(registerDoctorRequest.birthDate())
                .pesel(registerDoctorRequest.pesel())
                .phoneNumber(registerDoctorRequest.phoneNumber())
                .description(registerDoctorRequest.description())
                .education(registerDoctorRequest.education())
                .experience(registerDoctorRequest.experience())
                .pwzNumber(registerDoctorRequest.pwzNumber())
                .emailVerified(true)
                .specializations(new HashSet<>(specializationsFromDb))
                .build();
        userRepository.save(doctor);
        applicationEventPublisher.publishEvent(new DoctorRegisteredEvent(doctor, randomPassword));
    }

    @Override
    @Transactional
    public void registerPatient(RegisterPatientRequest registerPatientRequest) {
        passwordValidator.validatePassword(registerPatientRequest.password());
        Patient patient = Patient.builder()
                .name(registerPatientRequest.name())
                .surname(registerPatientRequest.surname())
                .email(registerPatientRequest.email())
                .password(passwordEncoder.encode(registerPatientRequest.password()))
                .role(UserRole.PATIENT)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .birthdate(registerPatientRequest.birthDate())
                .pesel(registerPatientRequest.pesel())
                .phoneNumber(registerPatientRequest.phoneNumber())
                .build();
        userRepository.save(patient);
        Token emailVerificationtoken = tokenService.createToken(patient, TokenName.EMAIL_VERIFICATION,
                emailVerificationTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new PatientRegisteredEvent(patient, emailVerificationtoken));
    }

    @Override
    @Transactional
    public void registerEmployee(RegisterEmployeeRequest registerEmployeeRequest) {
        String randomPassword = tokenService.createRandomToken().substring(0, 10);
        User employee = User.builder()
                .name(registerEmployeeRequest.name())
                .surname(registerEmployeeRequest.surname())
                .email(registerEmployeeRequest.email())
                .password(passwordEncoder.encode(randomPassword))
                .role(UserRole.EMPLOYEE)
                .birthdate(registerEmployeeRequest.birthDate())
                .pesel(registerEmployeeRequest.pesel())
                .phoneNumber(registerEmployeeRequest.phoneNumber())
                .emailVerified(true)
                .build();
        userRepository.save(employee);
        applicationEventPublisher.publishEvent(new EmployeeRegisteredEvent(employee, randomPassword));
    }

    @Override
    public void createVerifyEmailToken(VerifyEmailTokenRequest verifyEmailTokenRequest) {
        User userFromDb = userRepository.findByEmail(verifyEmailTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (userFromDb.isEmailVerified()) {
            throw new ApiException("Email is already verified", "email");
        }
        Token emailVerificationtoken = tokenService.createToken(userFromDb, TokenName.EMAIL_VERIFICATION,
                emailVerificationTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new EmailVerificationTokenCreatedEvent(userFromDb, emailVerificationtoken));
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        User userFromDb = userRepository.findByEmail(verifyEmailRequest.email());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (userFromDb.isEmailVerified()) {
            throw new ApiException("Email is already verified", "email");
        }
        tokenService.validateAndDeleteToken(userFromDb, verifyEmailRequest.token());
        userFromDb.setEmailVerified(true);
    }

    @Override
    @Transactional
    public void createNewPassword(Long staffMemberId) {
        User userFromDb = userRepository.findById(staffMemberId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (Objects.equals(userFromDb.getRole(), UserRole.PATIENT)) {
            throw new ApiException("User is not a staff member", "id");
        }
        String randomPassword = tokenService.createRandomToken().substring(0, 10);
        userFromDb.setPassword(passwordEncoder.encode(randomPassword));
        applicationEventPublisher.publishEvent(new StaffMemberPasswordCreatedEvent(userFromDb, randomPassword));
    }

}
