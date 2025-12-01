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

@SpringBootTest(classes = {DoctorDeleteOpinion.class})
@ActiveProfiles("test")
class DoctorDeleteOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    DoctorDeleteOpinion controller;

    @Test
    void shouldDeleteOpinionWhenDoctorOwnsIt() {
        Long doctorId = 1L;
        Long opinionId = 10L;

        Opinion op = new Opinion();
        Appointment appt = new Appointment();
        Doctor doc = new Doctor();
        doc.setId(doctorId);
        appt.setDoctor(doc);
        op.setAppointment(appt);

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        var resp = controller.delete(doctorId, opinionId);

        verify(opinionRepository).delete(op);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<String> body = resp.getBody();
        assertThat(body.getData()).isEqualTo("Opinion deleted successfully");
    }

    @Test
    void shouldThrowNotFoundWhenOpinionMissing() {
        when(opinionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(1L, 10L))
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

        assertThatThrownBy(() -> controller.delete(doctorId, opinionId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
    }
}
