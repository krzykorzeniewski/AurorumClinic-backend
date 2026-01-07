package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AdminDeleteOpinion.class})
@ActiveProfiles("test")
class AdminDeleteOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @Autowired
    AdminDeleteOpinion controller;

    @Test
    void shouldUnsetOpinionOnAppointmentWhenOpinionAndAppointmentExist() {
        Long opinionId = 1L;
        Long appointmentId = 10L;

        Opinion op = mock(Opinion.class);
        Appointment ap = mock(Appointment.class);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));
        when(op.getAppointment()).thenReturn(ap);
        when(ap.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(ap));

        controller.delete(opinionId);

        verify(opinionRepository).findById(opinionId);

        verify(op).getAppointment();
        verify(ap).getId();

        verify(appointmentRepository).findById(appointmentId);
        verify(ap).setOpinion(null);

        verify(opinionRepository, never()).delete(any());

        verifyNoMoreInteractions(opinionRepository, appointmentRepository, op, ap);
    }

    @Test
    void shouldThrowNotFoundWhenOpinionMissing() {
        Long opinionId = 999L;

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(opinionId))
                .isInstanceOf(ApiNotFoundException.class);

        verify(opinionRepository).findById(opinionId);
        verifyNoInteractions(appointmentRepository);
        verifyNoMoreInteractions(opinionRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentMissing() {
        Long opinionId = 1L;
        Long appointmentId = 10L;

        Opinion op = mock(Opinion.class);
        Appointment ap = mock(Appointment.class);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));
        when(op.getAppointment()).thenReturn(ap);
        when(ap.getId()).thenReturn(appointmentId);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(opinionId))
                .isInstanceOf(ApiNotFoundException.class);

        verify(opinionRepository).findById(opinionId);

        verify(op).getAppointment();
        verify(ap).getId();

        verify(appointmentRepository).findById(appointmentId);
        verify(ap, never()).setOpinion(any());

        verify(opinionRepository, never()).delete(any());

        verifyNoMoreInteractions(opinionRepository, appointmentRepository, op, ap);
    }
}
