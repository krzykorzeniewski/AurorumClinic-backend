package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PatientDeleteAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class PatientDeleteAppointmentTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @Autowired
    PatientDeleteAppointment patientDeleteAppointment;

    @Test
    void deleteAppointmentShouldThrowApiNotFoundExceptionWhenAppointmentIdIsNotFound() {
        Long patientId = 1L;
        Long appointmentId = 1L;

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                patientDeleteAppointment.deleteAppointment(appointmentId, patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void deleteAppointmentShouldThrowApiAuthExceptionWhenAppointmentPatientIdIsNotEqualRequestPatientId() {
        Long patientId = 1L;
        Long appointmentId = 1L;

        Patient testPatient = Patient.builder()
                .id(2L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .patient(testPatient)
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() ->
                patientDeleteAppointment.deleteAppointment(appointmentId, patientId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void deleteAppointmentShouldThrowApiExceptionWhenAppointmentStatusFinished() {
        Long patientId = 1L;
        Long appointmentId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .patient(testPatient)
                .status(AppointmentStatus.FINISHED)
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() ->
                patientDeleteAppointment.deleteAppointment(appointmentId, patientId))
                .isExactlyInstanceOf(ApiException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void deleteAppointmentShouldDeleteAndPublishEventWhenIdExists(
            @Autowired ApplicationEvents applicationEvents
            ) {
        Long patientId = 1L;
        Long appointmentId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .patient(testPatient)
                .status(AppointmentStatus.CREATED)
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        patientDeleteAppointment.deleteAppointment(appointmentId, patientId);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository).delete(appointmentArgumentCaptor.capture());

        Appointment appointmentToBeDeleted = appointmentArgumentCaptor.getValue();
        assertThat(appointmentToBeDeleted).isEqualTo(testAppointment);
        verify(appointmentRepository).findById(appointmentId);

        assertThat(applicationEvents.stream(AppointmentDeletedEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.getAppointment(), testAppointment) &&
                        Objects.equals(event.getPatient(), testPatient))
                .hasSize(1);
    }


}
