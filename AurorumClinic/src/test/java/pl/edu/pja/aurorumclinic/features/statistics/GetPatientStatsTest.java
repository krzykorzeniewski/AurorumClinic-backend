package pl.edu.pja.aurorumclinic.features.statistics;

import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.statistics.GetPatientStats.GetPatientStatsResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetPatientStats.class})
@ActiveProfiles("test")
class GetPatientStatsTest {

    @MockitoBean
    PatientRepository patientRepository;

    @Autowired
    GetPatientStats controller;

    @Test
    void shouldReturnStatsFromRepository() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 1, 31, 23, 59);

        Tuple tuple = mock(Tuple.class);
        when(tuple.get("totalRegistered")).thenReturn(100L);
        when(tuple.get("registeredThisPeriod")).thenReturn(25L);
        when(tuple.get("subscribedToNewsletter")).thenReturn(60L);

        when(patientRepository.getPatientStats(startedAt, finishedAt))
                .thenReturn(tuple);

        var resp = controller.getPatientStats(startedAt, finishedAt);

        assertThat(resp).isNotNull();
        ApiResponse<GetPatientStatsResponse> body = resp.getBody();
        assertThat(body).isNotNull();

        GetPatientStatsResponse data = body.getData();
        assertThat(data).isNotNull();
        assertThat(data.registered()).isEqualTo(100L);
        assertThat(data.registeredThisPeriod()).isEqualTo(25L);
        assertThat(data.subscribedToNewsletter()).isEqualTo(60L);

        verify(patientRepository).getPatientStats(startedAt, finishedAt);
    }
}
