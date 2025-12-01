package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PatientDeleteOpinion.class})
@ActiveProfiles("test")
class PatientDeleteOpinionTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    PatientDeleteOpinion controller;

    @Test
    void shouldDeleteOpinionWhenExistsAndBelongsToPatient() {
        Long userId = 1L;
        Long appointmentId = 10L;

        Appointment appt = new Appointment();
        appt.setId(appointmentId);
        Opinion op = new Opinion();
        op.setId(5L);
        appt.setOpinion(op);

        when(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId))
                .thenReturn(appt);

        var resp = controller.delete(userId, appointmentId);

        verify(opinionRepository).delete(op);
        assertThat(appt.getOpinion()).isNull();

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Boolean> body = resp.getBody();
        assertThat(body.getData()).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentNotBelongsToPatient() {
        Long userId = 1L;
        Long appointmentId = 10L;

        when(appointmentRepository.getAppointmentByIdAndPatientId(anyLong(), anyLong()))
                .thenReturn(null);

        assertThatThrownBy(() -> controller.delete(userId, appointmentId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenOpinionDoesNotExist() {
        Long userId = 1L;
        Long appointmentId = 10L;

        Appointment appt = new Appointment();
        appt.setId(appointmentId);
        appt.setOpinion(null);

        when(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId))
                .thenReturn(appt);

        assertThatThrownBy(() -> controller.delete(userId, appointmentId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }
}
