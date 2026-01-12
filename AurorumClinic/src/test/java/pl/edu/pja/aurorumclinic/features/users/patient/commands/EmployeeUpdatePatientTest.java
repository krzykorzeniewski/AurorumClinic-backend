package pl.edu.pja.aurorumclinic.features.users.patient.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.patients.commands.EmployeeUpdatePatient;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmployeeUpdatePatient.class})
@ActiveProfiles("test")
class EmployeeUpdatePatientTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    EmployeeUpdatePatient controller;

    @Test
    void updatePatientShouldThrowApiNotFoundExceptionWhenPatientNotFound() {
        Long patientId = 1L;

        EmployeeUpdatePatient.PutPatientRequest request = new EmployeeUpdatePatient.PutPatientRequest(
                "Jan",
                "Nowak",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "500600700",
                "jan.nowak@example.com",
                true
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updatePatient(patientId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(patientRepository).findById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void updatePatientShouldSetTwoFactorAuthFalseWhenPhoneNumberChanged_andReturnResponse() {
        Long patientId = 1L;

        Patient patientFromDb = Patient.builder()
                .id(patientId)
                .name("Jan")
                .surname("Nowak")
                .pesel("12345678901")
                .birthdate(LocalDate.of(1990, 1, 1))
                .phoneNumber("500600700")
                .email("jan.old@example.com")
                .twoFactorAuth(true)
                .newsletter(false)
                .emailVerified(true)
                .phoneNumberVerified(true)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .build();

        EmployeeUpdatePatient.PutPatientRequest request = new EmployeeUpdatePatient.PutPatientRequest(
                "Jan",
                "Nowak",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "777888999",
                "jan.new@example.com",
                true
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patientFromDb));

        var responseEntity = controller.updatePatient(patientId, request);
        Object apiResponse = responseEntity.getBody();

        EmployeeUpdatePatient.PutPatientResponse dto =
                (EmployeeUpdatePatient.PutPatientResponse) extractApiResponseData(apiResponse);

        assertThat(patientFromDb.getPhoneNumber()).isEqualTo("777888999");
        assertThat(patientFromDb.isTwoFactorAuth()).isFalse();

        assertThat(dto.id()).isEqualTo(patientId);
        assertThat(dto.name()).isEqualTo("Jan");
        assertThat(dto.surname()).isEqualTo("Nowak");
        assertThat(dto.pesel()).isEqualTo("12345678901");
        assertThat(dto.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(dto.phoneNumber()).isEqualTo("777888999");
        assertThat(dto.email()).isEqualTo("jan.new@example.com");
        assertThat(dto.newsletter()).isTrue();
        assertThat(dto.twoFactorAuth()).isFalse();
        assertThat(dto.communicationPreferences()).isEqualTo("EMAIL");

        verify(patientRepository).findById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void updatePatientShouldNotDisableTwoFactorAuthWhenPhoneNumberUnchanged() {
        Long patientId = 1L;

        Patient patientFromDb = Patient.builder()
                .id(patientId)
                .name("Anna")
                .surname("Kowalska")
                .pesel("10987654321")
                .birthdate(LocalDate.of(1985, 5, 5))
                .phoneNumber("500600700")
                .email("anna.old@example.com")
                .twoFactorAuth(true)
                .newsletter(false)
                .emailVerified(false)
                .phoneNumberVerified(false)
                .communicationPreferences(CommunicationPreference.PHONE_NUMBER)
                .build();

        EmployeeUpdatePatient.PutPatientRequest request = new EmployeeUpdatePatient.PutPatientRequest(
                "Anna",
                "Kowalska",
                "10987654321",
                LocalDate.of(1985, 5, 5),
                "500600700",
                "anna.new@example.com",
                false
        );

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patientFromDb));

        controller.updatePatient(patientId, request);

        assertThat(patientFromDb.isTwoFactorAuth()).isTrue();

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