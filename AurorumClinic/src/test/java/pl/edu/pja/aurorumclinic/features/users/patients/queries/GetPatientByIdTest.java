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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetPatientById.class})
@ActiveProfiles("test")
class GetPatientByIdTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    GetPatientById controller;

    @Test
    void getPatientByIdShouldThrowApiNotFoundExceptionWhenRepositoryReturnsNull() {
        Long patientId = 1L;

        when(patientRepository.getPatientResponseDtoById(patientId)).thenReturn(null);

        assertThatThrownBy(() -> controller.getPatientById(patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(patientRepository).getPatientResponseDtoById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientByIdShouldReturnDtoWhenFound() {
        Long patientId = 1L;

        GetPatientResponse dto = GetPatientResponse.builder().build();

        when(patientRepository.getPatientResponseDtoById(patientId)).thenReturn(dto);

        ResponseEntity<ApiResponse<GetPatientResponse>> responseEntity =
                controller.getPatientById(patientId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isEqualTo(dto);

        verify(patientRepository).getPatientResponseDtoById(patientId);
        verifyNoMoreInteractions(patientRepository);
    }
}
