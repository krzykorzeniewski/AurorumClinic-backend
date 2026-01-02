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
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {PatientDeleteOpinion.class})
@ActiveProfiles("test")
class PatientDeleteOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    PatientDeleteOpinion controller;

    @Test
    void shouldDeleteOpinionWhenExistsAndBelongsToPatient() {
        Long userId = 1L;
        Long appointmentId = 10L;
        Long opinionId = 20L;

        Patient patient = Patient.builder()
                .id(userId)
                .build();

        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .patient(patient)
                .build();

        Opinion opinion = Opinion.builder()
                .id(opinionId)
                .appointment(appointment)
                .build();

        when(opinionRepository.findById(opinionId))
                .thenReturn(Optional.of(opinion));

        var resp = controller.delete(userId, opinionId);

        verify(opinionRepository).delete(opinion);
        assertThat(appointment.getOpinion()).isNull();

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Boolean> body = resp.getBody();
        assertThat(body.getData()).isTrue();
    }

    @Test
    void shouldThrowAuthorizationExceptionWhenAppointmentNotBelongsToPatient() {
        Long userId = 1L;
        Long appointmentId = 10L;
        Long opinionId = 20L;

        Patient patient = Patient.builder()
                .id(userId)
                .build();

        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .patient(patient)
                .build();

        Opinion opinion = Opinion.builder()
                .id(opinionId)
                .appointment(Appointment.builder()
                        .patient(Patient.builder()
                                .id(100L)
                                .build())
                        .build())
                .build();

        appointment.setOpinion(Opinion.builder()
                .id(15L)
                .build());

        when(opinionRepository.findById(opinionId))
                .thenReturn(Optional.of(opinion));

        assertThatThrownBy(() -> controller.delete(userId, opinionId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
    }

    @Test
    void shouldThrowNotFoundWhenOpinionDoesNotExist() {
        Long userId = 1L;
        Long opinionId = 10L;

        when(opinionRepository.findById(opinionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(userId, opinionId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
    }
}
