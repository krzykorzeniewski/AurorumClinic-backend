package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PatientUpdateOpinion.class})
@ActiveProfiles("test")
class PatientUpdateOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @MockitoBean
    ContentModerationService contentModerationService;

    @Autowired
    PatientUpdateOpinion controller;

    @Test
    void shouldUpdateOpinionWhenBelongsToPatient() {
        Long userId = 1L;
        Long opinionId = 10L;

        var req = new PatientUpdateOpinion.Request(3, "zaktualizowany komentarz");

        Opinion op = new Opinion();
        op.setId(opinionId);
        Appointment appt = new Appointment();
        Patient p = new Patient();
        p.setId(userId);
        appt.setPatient(p);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        var resp = controller.update(userId, opinionId, req);

        verify(contentModerationService).assertAllowed("zaktualizowany komentarz", "comment");

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<PatientUpdateOpinion.Response> body = resp.getBody();
        assertThat(body.getData()).isNotNull();
        var data = body.getData();

        assertThat(data.opinionId()).isEqualTo(opinionId);
        assertThat(data.rating()).isEqualTo(3);
        assertThat(data.comment()).isEqualTo("zaktualizowany komentarz");
        assertThat(op.getRating()).isEqualTo(3);
        assertThat(op.getComment()).isEqualTo("zaktualizowany komentarz");
    }

    @Test
    void shouldThrowNotFoundWhenOpinionDoesNotExist() {
        Long userId = 1L;
        Long opinionId = 10L;
        var req = new PatientUpdateOpinion.Request(4, "coś");

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.update(userId, opinionId, req))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowAuthorizationWhenOpinionNotBelongsToPatient() {
        Long userId = 1L;
        Long opinionId = 10L;
        var req = new PatientUpdateOpinion.Request(4, "coś");

        Opinion op = new Opinion();
        Appointment appt = new Appointment();
        Patient p = new Patient();
        p.setId(999L);
        appt.setPatient(p);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        assertThatThrownBy(() -> controller.update(userId, opinionId, req))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
    }
}
