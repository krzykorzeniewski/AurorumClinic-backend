package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DoctorDeleteAnswer.class})
@ActiveProfiles("test")
class DoctorDeleteAnswerTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    DoctorDeleteAnswer controller;

    @Test
    void shouldClearAnswerWhenDoctorOwnsOpinionAndAnswerExists() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        Opinion op = new Opinion();
        op.setAnswer("stara odpowiedź");
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(doctorId);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        var resp = controller.deleteAnswer(doctorId, opinionId);

        assertThat(op.getAnswer()).isNull();
        assertThat(resp.getBody()).isNotNull();
        ApiResponse<String> body = resp.getBody();
        assertThat(body.getData()).isEqualTo("Answer deleted successfully");
    }

    @Test
    void shouldThrowNotFoundWhenOpinionMissing() {
        when(opinionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.deleteAnswer(1L, 10L))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenAnswerNull() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        Opinion op = new Opinion();
        op.setAnswer(null);
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(doctorId);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> controller.deleteAnswer(doctorId, opinionId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowAuthorizationWhenDifferentDoctor() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        Opinion op = new Opinion();
        op.setAnswer("coś");
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(999L);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> controller.deleteAnswer(doctorId, opinionId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
    }
}
