package pl.edu.pja.aurorumclinic.features.opinions.quries;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.opinions.queries.GetDoctorOpinions;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetDoctorOpinions.class})
@ActiveProfiles("test")
class GetDoctorOpinionsTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    GetDoctorOpinions controller;

    @Test
    void shouldReturnOpinionsAndAverageRating() {
        Long doctorId = 1L;

        Opinion o1 = new Opinion();
        o1.setId(1L);
        o1.setRating(5);
        o1.setComment("super");
        o1.setCreatedAt(LocalDateTime.now());

        Opinion o2 = new Opinion();
        o2.setId(2L);
        o2.setRating(3);
        o2.setComment("ok");
        o2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(opinionRepository.findByAppointment_Doctor_IdOrderByCreatedAtDesc(doctorId))
                .thenReturn(List.of(o1, o2));

        var resp = controller.list(doctorId);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<GetDoctorOpinions.Response> body = resp.getBody();
        GetDoctorOpinions.Response data = body.getData();

        assertThat(data.total()).isEqualTo(2);
        assertThat(data.opinions()).hasSize(2);
        assertThat(data.averageRating()).isEqualTo(4.0);
    }
}