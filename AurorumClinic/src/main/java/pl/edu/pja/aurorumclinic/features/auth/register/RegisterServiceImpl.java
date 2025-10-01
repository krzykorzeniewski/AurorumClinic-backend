package pl.edu.pja.aurorumclinic.features.auth.register;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.register.dtos.*;
import pl.edu.pja.aurorumclinic.features.auth.register.events.UserRegisteredEvent;
import pl.edu.pja.aurorumclinic.features.auth.register.events.VerifyAccountMessageRequestedEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void registerDoctor(RegisterDoctorRequest registerDoctorRequest) {
        if (userRepository.findByEmail(registerDoctorRequest.email()) != null) {
            throw new ApiConflictException("Email already in use", "email");
        }
        Doctor doctor = Doctor.builder()
                .name(registerDoctorRequest.name())
                .surname(registerDoctorRequest.surname())
                .email(registerDoctorRequest.email())
                .password(passwordEncoder.encode(registerDoctorRequest.password()))
                .role(UserRole.DOCTOR)
                .birthdate(registerDoctorRequest.birthDate())
                .pesel(registerDoctorRequest.pesel())
                .phoneNumber(registerDoctorRequest.phoneNumber())
                .description(registerDoctorRequest.description())
                .specialization(registerDoctorRequest.specialization())
                .education(registerDoctorRequest.education())
                .experience(registerDoctorRequest.experience())
                .pwzNumber(registerDoctorRequest.pwzNumber())
                .build();
        userRepository.save(doctor);
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(doctor));
    }

    @Override
    @Transactional
    public void registerPatient(RegisterPatientRequest registerPatientRequest) {
        if (userRepository.findByEmail(registerPatientRequest.email()) != null) {
            throw new ApiConflictException("Email already in use", "email");
        }
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
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(patient));
    }

    @Override
    @Transactional
    public void registerEmployee(RegisterEmployeeRequest registerEmployeeRequest) {
        if (userRepository.findByEmail(registerEmployeeRequest.email()) != null) {
            throw new ApiConflictException("Email already in use", "email");
        }
        User employee = User.builder()
                .name(registerEmployeeRequest.name())
                .surname(registerEmployeeRequest.surname())
                .email(registerEmployeeRequest.email())
                .password(passwordEncoder.encode(registerEmployeeRequest.password()))
                .role(UserRole.EMPLOYEE)
                .birthdate(registerEmployeeRequest.birthDate())
                .pesel(registerEmployeeRequest.pesel())
                .phoneNumber(registerEmployeeRequest.phoneNumber())
                .build();
        userRepository.save(employee);
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(employee));
    }

    @Override
    public void sendVerifyEmail(VerifyEmailTokenRequest verifyEmailTokenRequest) {
        User userFromDb = userRepository.findByEmail(verifyEmailTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiNotFoundException("Email not found", "email");
        }
        if (userFromDb.isEmailVerified()) {
            throw new ApiException("Email is already verified", "email");
        }
        applicationEventPublisher.publishEvent(new VerifyAccountMessageRequestedEvent(userFromDb));
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

}
