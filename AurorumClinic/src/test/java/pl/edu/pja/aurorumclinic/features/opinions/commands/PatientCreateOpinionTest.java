package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PatientCreateOpinion.class})
@ActiveProfiles("test")
class PatientCreateOpinionTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    OpinionRepository opinionRepository;

    @MockitoBean
    ContentModerationService contentModerationService;

    @Autowired
    PatientCreateOpinion controller;

    @Test
    void shouldCreateOpinionAndAttachToAppointment() {
        Long userId = 1L;
        Long appointmentId = 10L;

        PatientCreateOpinion.Request req =
                new PatientCreateOpinion.Request(5, "Bardzo dobry lekarz");

        Appointment appt = new Appointment();
        appt.setId(appointmentId);
        appt.setFinishedAt(LocalDateTime.now().minusMinutes(5));
        Doctor d = new Doctor();
        d.setId(100L);
        appt.setDoctor(d);

        when(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId))
                .thenReturn(appt);

        when(opinionRepository.save(any(Opinion.class))).thenAnswer(invocation -> {
            Opinion o = invocation.getArgument(0);
            o.setId(123L);
            return o;
        });

        controller.create(userId, appointmentId, req);

        verify(contentModerationService).assertAllowed("Bardzo dobry lekarz", "comment");

        ArgumentCaptor<Opinion> captor = ArgumentCaptor.forClass(Opinion.class);
        verify(opinionRepository).save(captor.capture());
        Opinion saved = captor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getRating()).isEqualTo(5);
        assertThat(saved.getComment()).isEqualTo("Bardzo dobry lekarz");
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(appt.getOpinion()).isSameAs(saved);
        assertThat(appt.getDoctor().getId()).isEqualTo(100L);
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentNotBelongsToPatient() {
        Long userId = 1L;
        Long appointmentId = 10L;
        var req = new PatientCreateOpinion.Request(4, "ok");

        when(appointmentRepository.getAppointmentByIdAndPatientId(anyLong(), anyLong()))
                .thenReturn(null);

        assertThatThrownBy(() -> controller.create(userId, appointmentId, req))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowConflictWhenAppointmentNotFinished() {
        Long userId = 1L;
        Long appointmentId = 10L;
        var req = new PatientCreateOpinion.Request(4, "ok");

        Appointment appt = new Appointment();
        appt.setId(appointmentId);
        appt.setFinishedAt(null);

        when(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId))
                .thenReturn(appt);

        assertThatThrownBy(() -> controller.create(userId, appointmentId, req))
                .isExactlyInstanceOf(ApiConflictException.class);
    }

    @Test
    void shouldThrowConflictWhenOpinionAlreadyExists() {
        Long userId = 1L;
        Long appointmentId = 10L;
        var req = new PatientCreateOpinion.Request(4, "ok");

        Appointment appt = new Appointment();
        appt.setId(appointmentId);
        appt.setFinishedAt(LocalDateTime.now().minusMinutes(5));
        appt.setOpinion(new Opinion());

        when(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId))
                .thenReturn(appt);

        assertThatThrownBy(() -> controller.create(userId, appointmentId, req))
                .isExactlyInstanceOf(ApiConflictException.class);
    }
}
