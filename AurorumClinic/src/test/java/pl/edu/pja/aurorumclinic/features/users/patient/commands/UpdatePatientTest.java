package pl.edu.pja.aurorumclinic.features.users.patient.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.patients.commands.UpdatePatient;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdatePatient.class})
@ActiveProfiles("test")
class UpdatePatientTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    UpdatePatient controller;

    @Test
    void partiallyUpdatePatientShouldThrowApiNotFoundExceptionWhenPatientNotFound() {
        Long patientId = 1L;

        UpdatePatient.PatchPatientRequest request = new UpdatePatient.PatchPatientRequest(
                CommunicationPreference.EMAIL,
                true
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.partiallyUpdatePatient(patientId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(patientRepository).findById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void partiallyUpdatePatientShouldThrowApiExceptionWhenPhoneNotVerifiedAndPreferenceIsPhoneNumber() {
        Long patientId = 1L;

        Patient patientFromDb = Patient.builder()
                .id(patientId)
                .name("Jan")
                .surname("Nowak")
                .pesel("12345678901")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("500600700")
                .email("jan.nowak@example.com")
                .twoFactorAuth(false)
                .newsletter(false)
                .emailVerified(true)
                .phoneNumberVerified(false)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .build();

        UpdatePatient.PatchPatientRequest request = new UpdatePatient.PatchPatientRequest(
                CommunicationPreference.PHONE_NUMBER,
                true
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patientFromDb));

        assertThatThrownBy(() -> controller.partiallyUpdatePatient(patientId, request))
                .isExactlyInstanceOf(ApiException.class);

        verify(patientRepository).findById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void partiallyUpdatePatientShouldUpdatePreferencesAndNewsletter_andReturnResponse() {
        Long patientId = 1L;

        Patient patientFromDb = Patient.builder()
                .id(patientId)
                .name("Anna")
                .surname("Kowalska")
                .pesel("10987654321")
                .birthdate(LocalDate.of(1985, 5, 5))
                .phoneNumber("500600700")
                .email("anna.kowalska@example.com")
                .twoFactorAuth(true)
                .newsletter(false)
                .emailVerified(false)
                .phoneNumberVerified(true)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .build();

        UpdatePatient.PatchPatientRequest request = new UpdatePatient.PatchPatientRequest(
                CommunicationPreference.PHONE_NUMBER,
                true
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patientFromDb));

        var responseEntity = controller.partiallyUpdatePatient(patientId, request);
        Object apiResponse = responseEntity.getBody();

        UpdatePatient.PatchPatientResponse dto =
                (UpdatePatient.PatchPatientResponse) extractApiResponseData(apiResponse);

        assertThat(patientFromDb.getCommunicationPreferences()).isEqualTo(CommunicationPreference.PHONE_NUMBER);
        assertThat(patientFromDb.isNewsletter()).isTrue();

        assertThat(dto.id()).isEqualTo(patientId);
        assertThat(dto.name()).isEqualTo("Anna");
        assertThat(dto.surname()).isEqualTo("Kowalska");
        assertThat(dto.pesel()).isEqualTo("10987654321");
        assertThat(dto.birthDate()).isEqualTo(LocalDate.of(1985, 5, 5));
        assertThat(dto.phoneNumber()).isEqualTo("500600700");
        assertThat(dto.email()).isEqualTo("anna.kowalska@example.com");
        assertThat(dto.twoFactorAuth()).isTrue();
        assertThat(dto.newsletter()).isTrue();
        assertThat(dto.communicationPreferences()).isEqualTo("PHONE_NUMBER");
        assertThat(dto.phoneNumberVerified()).isTrue();
        assertThat(dto.emailVerified()).isFalse();

        verify(patientRepository).findById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    private static Object extractApiResponseData(Object apiResponse) {
        if (apiResponse == null) return null;
        for (String methodName : List.of("data", "getData", "result", "getResult", "payload", "getPayload")) {
            try {
                Method m = apiResponse.getClass().getMethod(methodName);
                return m.invoke(apiResponse);
            } catch (Exception ignored) { }
        }
        fail("Nie udało się wyciągnąć data/result z ApiResponse. Jeśli pokażesz ApiResponse, dopasuję asercje 1:1.");
        return null;
    }
}
