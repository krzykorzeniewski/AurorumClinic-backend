package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PatientRescheduleAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class PatientRescheduleAppointmentTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    AppointmentValidator appointmentValidator;

    @Autowired
    PatientRescheduleAppointment patientRescheduleAppointment;

    @Test
    void updateAppointmentShouldThrowApiNotFoundExceptionWhenAppointmentIdIsNotFound() {
        Long patientId = 1L;
        Long appointmentId = 1L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request = new PatientRescheduleAppointment
                .PatientUpdateAppointmentRequest(LocalDateTime.now(), "opis");

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientRescheduleAppointment.updateAppointment(request, patientId, appointmentId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void updateAppointmentShouldThrowApiAuthExceptionWhenAppointmentPatientIdDoesNotMatchId() {
        Long patientId = 1L;
        Long appointmentId = 1L;
        Patient testPatient = Patient.builder()
                .id(2L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .patient(testPatient)
                .build();

        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request = new PatientRescheduleAppointment
                .PatientUpdateAppointmentRequest(LocalDateTime.now(), "opis");

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() -> patientRescheduleAppointment.updateAppointment(request, patientId, appointmentId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(appointmentRepository).findById(patientId);
    }

    @Test
    void updateAppointmentShouldUpdateAppointmentAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents) {
        Long patientId = 1L;
        Long appointmentId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .patient(testPatient)
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .service(Service.builder()
                        .id(1L)
                        .duration(30)
                        .build())
                .build();
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request = new PatientRescheduleAppointment
                .PatientUpdateAppointmentRequest(LocalDateTime.now(), "opis");
        LocalDateTime newFinishedAt = request.startedAt().plusMinutes(testAppointment.getService().getDuration());

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        patientRescheduleAppointment.updateAppointment(request, patientId, appointmentId);

        assertThat(testAppointment.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(testAppointment.getFinishedAt()).isEqualTo(newFinishedAt);
        assertThat(testAppointment.getDescription()).isEqualTo(request.description());
        verify(appointmentValidator).validateRescheduledAppointment(request.startedAt(), newFinishedAt,
                testAppointment.getDoctor(), testAppointment.getService(), testAppointment);

        assertThat(applicationEvents.stream(AppointmentRescheduledEvent.class))
                .filteredOn(event -> Objects.equals(event.getAppointment(), testAppointment)
                        && Objects.equals(event.getPatient(), testPatient))
                .hasSize(1);
    }
}
