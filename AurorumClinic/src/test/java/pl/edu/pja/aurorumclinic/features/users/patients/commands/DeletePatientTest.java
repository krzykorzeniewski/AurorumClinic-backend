package pl.edu.pja.aurorumclinic.features.users.patients.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DeletePatient.class})
@ActiveProfiles("test")
class DeletePatientTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    DeletePatient deletePatient;

    @Test
    void deleteAccountShouldThrowApiNotFoundExceptionWhenPatientNotFound() {
        Long patientId = 1L;

        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deletePatient.deleteAccount(patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(patientRepository).findById(patientId);
        verify(patientRepository, never()).delete(any(Patient.class));
    }

    @Test
    void deleteAccountShouldDeletePatientWhenFound() {
        Long patientId = 1L;

        Patient patientFromDb = Patient.builder()
                .id(patientId)
                .build();

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patientFromDb));

        deletePatient.deleteAccount(patientId);

        verify(patientRepository).findById(patientId);
        verify(patientRepository).delete(patientFromDb);
    }
}
