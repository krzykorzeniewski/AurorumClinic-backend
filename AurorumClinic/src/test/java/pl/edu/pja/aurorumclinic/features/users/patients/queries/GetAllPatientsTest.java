package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetAllPatients.class})
@ActiveProfiles("test")
class GetAllPatientsTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    GetAllPatients controller;

    @Test
    void getAllPatientsShouldUseFindAllGetPatientDtosWhenQueryIsNull() {
        String query = null;
        Pageable pageable = PageRequest.of(0, 5);

        GetPatientResponse dto1 = GetPatientResponse.builder().build();
        GetPatientResponse dto2 = GetPatientResponse.builder().build();
        Page<GetPatientResponse> pageFromDb = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(patientRepository.findAllGetPatientDtos(pageable)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetPatientResponse>>> responseEntity =
                controller.getAllPatients(query, pageable);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isEqualTo(pageFromDb);
        assertThat(responseEntity.getBody().getData().getContent()).hasSize(2);

        verify(patientRepository).findAllGetPatientDtos(pageable);
        verify(patientRepository, never()).searchAllByQuery(anyString(), any(Pageable.class));
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getAllPatientsShouldUseSearchAllByQueryWhenQueryProvided() {
        String query = "kow";
        Pageable pageable = PageRequest.of(0, 5);

        GetPatientResponse dto = GetPatientResponse.builder().build();
        Page<GetPatientResponse> pageFromDb = new PageImpl<>(List.of(dto), pageable, 1);

        when(patientRepository.searchAllByQuery(query, pageable)).thenReturn(pageFromDb);

        ResponseEntity<ApiResponse<Page<GetPatientResponse>>> responseEntity =
                controller.getAllPatients(query, pageable);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getData()).isEqualTo(pageFromDb);
        assertThat(responseEntity.getBody().getData().getContent()).hasSize(1);

        verify(patientRepository).searchAllByQuery(query, pageable);
        verify(patientRepository, never()).findAllGetPatientDtos(any(Pageable.class));
        verifyNoMoreInteractions(patientRepository);
    }
}
