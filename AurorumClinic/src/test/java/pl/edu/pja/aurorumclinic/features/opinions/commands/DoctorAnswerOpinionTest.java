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
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DoctorAnswerOpinion.class})
@ActiveProfiles("test")
class DoctorAnswerOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @MockitoBean
    ContentModerationService contentModerationService;

    @Autowired
    DoctorAnswerOpinion controller;

    @Test
    void shouldSetAnswerWhenDoctorOwnsOpinion() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        var req = new DoctorAnswerOpinion.Request("Dziękuję za opinię");

        Opinion op = new Opinion();
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(doctorId);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        var resp = controller.answer(doctorId, opinionId, req);

        verify(contentModerationService).assertAllowed("Dziękuję za opinię", "answer");

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<String> body = resp.getBody();
        assertThat(body.getData()).isEqualTo("Answer added successfully");
        assertThat(op.getAnswer()).isEqualTo("Dziękuję za opinię");
    }

    @Test
    void shouldThrowNotFoundWhenOpinionMissing() {
        when(opinionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.answer(1L, 10L,
                new DoctorAnswerOpinion.Request("tekst")))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowAuthorizationWhenDifferentDoctor() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        Opinion op = new Opinion();
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(999L);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> controller.answer(doctorId, opinionId,
                new DoctorAnswerOpinion.Request("tekst")))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
    }
}
