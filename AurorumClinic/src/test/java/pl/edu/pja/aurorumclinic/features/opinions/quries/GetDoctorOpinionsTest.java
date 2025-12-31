package pl.edu.pja.aurorumclinic.features.opinions.quries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.opinions.queries.GetDoctorOpinions;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetDoctorOpinions.class})
@ActiveProfiles("test")
class GetDoctorOpinionsTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    GetDoctorOpinions controller;

    @Test
    void shouldReturnPagedOpinionsAndMapFieldsCorrectly() {
        Long doctorId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        Opinion o1 = new Opinion();
        o1.setId(1L);
        o1.setRating(5);
        o1.setComment("super");
        o1.setAnswer("dzięki");
        o1.setCreatedAt(LocalDateTime.now());
        Patient p1 = Patient.builder()
                .id(2L)
                .name("Maurycy")
                .surname("Nowak")
                .build();
        Appointment a1 = new Appointment();
        a1.setId(100L);
        a1.setPatient(p1);
        o1.setAppointment(a1);

        Opinion o2 = new Opinion();
        o2.setId(2L);
        o2.setRating(3);
        o2.setComment("ok");
        o2.setAnswer(null);
        o2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Patient p2 = Patient.builder()
                .id(1L)
                .name("Mariusz")
                .surname("Kowalski")
                .build();
        Appointment a2 = new Appointment();
        a2.setPatient(p2);
        a2.setId(101L);
        o2.setAppointment(a2);

        Page<Opinion> page = new PageImpl<>(List.of(o1, o2), pageable, 2);

        when(opinionRepository.findByAppointment_Doctor_Id(doctorId, pageable))
                .thenReturn(page);
        var resp = controller.list(doctorId, pageable);
        assertThat(resp).isNotNull();
        ApiResponse<Page<GetDoctorOpinions.OpinionDto>> body = resp.getBody();
        assertThat(body).isNotNull();

        Page<GetDoctorOpinions.OpinionDto> data = body.getData();
        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getNumber()).isEqualTo(0);
        assertThat(data.getSize()).isEqualTo(10);

        assertThat(data.getContent()).hasSize(2);

        GetDoctorOpinions.OpinionDto dto1 = data.getContent().get(0);
        assertThat(dto1.id()).isEqualTo(1L);
        assertThat(dto1.rating()).isEqualTo(5);
        assertThat(dto1.comment()).isEqualTo("super");
        assertThat(dto1.answer()).isEqualTo("dzięki");

        GetDoctorOpinions.OpinionDto dto2 = data.getContent().get(1);
        assertThat(dto2.id()).isEqualTo(2L);
        assertThat(dto2.rating()).isEqualTo(3);
        assertThat(dto2.comment()).isEqualTo("ok");
        assertThat(dto2.answer()).isNull();
        assertThat(dto2.patient()).isNotNull();
        assertThat(dto2.patient().id()).isEqualTo(p2.getId());
        assertThat(dto2.patient().name()).isEqualTo(p2.getName());
        assertThat(dto2.patient().surname()).isEqualTo(p2.getSurname());

        verify(opinionRepository).findByAppointment_Doctor_Id(doctorId, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoOpinions() {
        Long doctorId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        when(opinionRepository.findByAppointment_Doctor_Id(doctorId, pageable))
                .thenReturn(Page.empty(pageable));

        var resp = controller.list(doctorId, pageable);

        assertThat(resp).isNotNull();
        ApiResponse<Page<GetDoctorOpinions.OpinionDto>> body = resp.getBody();
        assertThat(body).isNotNull();

        Page<GetDoctorOpinions.OpinionDto> data = body.getData();
        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isZero();
        assertThat(data.getContent()).isEmpty();

        verify(opinionRepository).findByAppointment_Doctor_Id(doctorId, pageable);
    }
}
