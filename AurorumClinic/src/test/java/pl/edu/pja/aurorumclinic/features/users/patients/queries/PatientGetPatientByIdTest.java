package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PatientGetPatientById.class})
@ActiveProfiles("test")
class PatientGetPatientByIdTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    PatientGetPatientById controller;

    @Test
    void getPatientShouldReturnDtoFromRepository() {
        Long patientId = 1L;

        GetPatientResponse dto = GetPatientResponse.builder().build();

        when(patientRepository.getPatientResponseDtoById(patientId)).thenReturn(dto);

        ResponseEntity<ApiResponse<GetPatientResponse>> responseEntity =
                controller.getPatient(patientId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isEqualTo(dto);

        verify(patientRepository).getPatientResponseDtoById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientShouldReturnNullWhenRepositoryReturnsNull() {
        Long patientId = 1L;

        when(patientRepository.getPatientResponseDtoById(patientId)).thenReturn(null);

        ResponseEntity<ApiResponse<GetPatientResponse>> responseEntity =
                controller.getPatient(patientId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isNull();

        verify(patientRepository).getPatientResponseDtoById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }
}
