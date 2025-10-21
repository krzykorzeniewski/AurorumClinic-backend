package pl.edu.pja.aurorumclinic.features.users.patients.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class EmployeeUpdatePatient {

    private final PatientRepository patientRepository;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Transactional
    public ResponseEntity<ApiResponse<PutPatientResponse>> updatePatient(@PathVariable Long id,
                                                                         @Valid @RequestBody PutPatientRequest requestDto) {
        return ResponseEntity.ok(ApiResponse.success(handle(id, requestDto)));
    }

    private PutPatientResponse handle(Long id, PutPatientRequest request) {
        Patient patientFromDb = patientRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        patientFromDb.setName(request.name());
        patientFromDb.setSurname(request.surname());
        patientFromDb.setPesel(request.pesel());
        patientFromDb.setBirthdate(request.birthdate());
        patientFromDb.setPhoneNumber(request.phoneNumber());
        patientFromDb.setEmail(request.email());
        patientFromDb.setCommunicationPreferences(request.communicationPreferences());
        patientFromDb.setNewsletter(request.newsletter());
        patientFromDb.setTwoFactorAuth(request.twoFactorAuth());

        return PutPatientResponse.builder()
                .id(patientFromDb.getId())
                .name(patientFromDb.getName())
                .surname(patientFromDb.getSurname())
                .pesel(patientFromDb.getPesel())
                .birthDate(patientFromDb.getBirthdate())
                .phoneNumber(patientFromDb.getPhoneNumber())
                .email(patientFromDb.getEmail())
                .twoFactorAuth(patientFromDb.isTwoFactorAuth())
                .newsletter(patientFromDb.isNewsletter())
                .communicationPreferences(patientFromDb.getCommunicationPreferences().name())
                .emailVerified(patientFromDb.isEmailVerified())
                .phoneNumberVerified(patientFromDb.isPhoneNumberVerified())
                .build();
    }

    public record PutPatientRequest(@NotBlank @Size(max = 50) String name,
                                    @NotBlank @Size(max = 50) String surname,
                                    @NotBlank @Size(min = 11, max = 11) String pesel,
                                    @NotNull LocalDate birthdate,
                                    @Size(min = 9, max = 9) @NotBlank String phoneNumber,
                                    @Email @NotBlank @Size(max = 100) String email,
                                    @NotNull CommunicationPreference communicationPreferences,
                                    boolean newsletter,
                                    boolean twoFactorAuth) {
    }

    @Builder
    public record PutPatientResponse(Long id,
                                     String name,
                                     String surname,
                                     String pesel,
                                     LocalDate birthDate,
                                     String email,
                                     String phoneNumber,
                                     boolean twoFactorAuth,
                                     boolean newsletter,
                                     boolean emailVerified,
                                     boolean phoneNumberVerified,
                                     String communicationPreferences) {
    }
}
