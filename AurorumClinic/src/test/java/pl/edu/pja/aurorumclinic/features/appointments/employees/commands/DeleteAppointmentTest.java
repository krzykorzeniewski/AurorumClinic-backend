package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DeleteAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class DeleteAppointmentTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @Autowired
    DeleteAppointment deleteAppointment;

    @Test
    void deleteAppointmentShouldThrowApiNotFoundExceptionWhenAppointmentIdNotFound() {
        Long appointmentId = 1L;

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteAppointment.deleteAppointment(appointmentId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void deleteAppointmentShouldThrowApiExceptionWhenAppointmentHasFinishedStatus() {
        Long appointmentId = 1L;
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .status(AppointmentStatus.FINISHED)
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() -> deleteAppointment.deleteAppointment(appointmentId))
                .isExactlyInstanceOf(ApiException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void deleteAppointmentShouldDeleteAppointmentAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
            ) {
        Long appointmentId = 1L;
        Patient testPatient = Patient.builder()
                .id(1L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .status(AppointmentStatus.CREATED)
                .patient(testPatient)
                .build();

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        deleteAppointment.deleteAppointment(appointmentId);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository).delete(appointmentArgumentCaptor.capture());

        Appointment deletedAppointment = appointmentArgumentCaptor.getValue();

        assertThat(deletedAppointment).isEqualTo(testAppointment);
        verify(appointmentRepository).findById(appointmentId);

        assertThat(applicationEvents.stream(AppointmentDeletedEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.getAppointment(), testAppointment)
                                && Objects.equals(event.getPatient(), testAppointment.getPatient()))
                .hasSize(1);
    }

}
