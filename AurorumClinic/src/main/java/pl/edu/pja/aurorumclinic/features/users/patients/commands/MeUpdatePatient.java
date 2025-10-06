package pl.edu.pja.aurorumclinic.features.users.patients.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class MeUpdatePatient {

    private final PatientRepository patientRepository;

    @PatchMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional
    public ResponseEntity<ApiResponse<PatchPatientResponse>> partiallyUpdatePatient(@AuthenticationPrincipal Long id,
                                                                  @Valid @RequestBody PatchPatientRequest requestDto) {
        return ResponseEntity.ok(ApiResponse.success(handle(id, requestDto)));
    }

    private PatchPatientResponse handle(Long id, PatchPatientRequest request) {
        Patient patientFromDb = patientRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!patientFromDb.isPhoneNumberVerified() &&
                Objects.equals(request.communicationPreferences(), CommunicationPreference.PHONE_NUMBER)) {
            throw new ApiException("phone number is not verified", "communicationPreferences");
        }
        patientFromDb.setCommunicationPreferences(request.communicationPreferences());
        patientFromDb.setNewsletter(request.newsletter());

        return PatchPatientResponse.builder()
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

    public record PatchPatientRequest(@NotNull CommunicationPreference communicationPreferences,
                                      boolean newsletter
    ) {
    }

    @Builder
    public record PatchPatientResponse(Long id,
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
