package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

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
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DeleteAppointmentBulk.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class DeleteAppointmentBulkTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @Autowired
    DeleteAppointmentBulk deleteAppointmentBulk;

    @Test
    void deleteAppointmentsInBulkShouldThrowApiNotFoundExceptionWhenAppointmentIdNotFound() {
        Long appointmentId1 = 1L;
        Long appointmentId2 = 2L;
        Appointment testAppointment1 = Appointment.builder()
                .id(appointmentId1)
                .status(AppointmentStatus.CREATED)
                .build();
        DeleteAppointmentBulk.DeleteAppointmentBulkRequest request = new
                DeleteAppointmentBulk.DeleteAppointmentBulkRequest(List.of(appointmentId1, appointmentId2));

        when(appointmentRepository.findAllById(any())).thenReturn(List.of(testAppointment1));

        assertThatThrownBy(() -> deleteAppointmentBulk.deleteAppointmentsInBulk(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).findAllById(request.appointmentIds());
    }

    @Test
    void deleteAppointmentsInBulkShouldThrowApiExceptionWhenAppointmentHasStatusFinished() {
        Long appointmentId1 = 1L;
        Long appointmentId2 = 2L;
        Appointment testAppointment1 = Appointment.builder()
                .id(appointmentId1)
                .status(AppointmentStatus.CREATED)
                .build();
        Appointment testAppointment2 = Appointment.builder()
                .id(appointmentId2)
                .status(AppointmentStatus.FINISHED)
                .build();

        DeleteAppointmentBulk.DeleteAppointmentBulkRequest request = new
                DeleteAppointmentBulk.DeleteAppointmentBulkRequest(List.of(appointmentId1, appointmentId2));

        when(appointmentRepository.findAllById(any())).thenReturn(List.of(testAppointment1, testAppointment2));

        assertThatThrownBy(() -> deleteAppointmentBulk.deleteAppointmentsInBulk(request))
                .isExactlyInstanceOf(ApiException.class);
        verify(appointmentRepository).findAllById(request.appointmentIds());
    }

    @Test
    void deleteAppointmentsInBulkShouldDeleteListOfAppointmentsAndPublishEvents(
            @Autowired ApplicationEvents applicationEvents
            ) {
        Long appointmentId1 = 1L;
        Long appointmentId2 = 2L;
        Long appointmentId3 = 3L;
        Appointment testAppointment1 = Appointment.builder()
                .id(appointmentId1)
                .status(AppointmentStatus.CREATED)
                .build();
        Appointment testAppointment2 = Appointment.builder()
                .id(appointmentId2)
                .status(AppointmentStatus.CREATED)
                .build();
        Appointment testAppointment3 = Appointment.builder()
                .id(appointmentId3)
                .status(AppointmentStatus.CREATED)
                .build();

        DeleteAppointmentBulk.DeleteAppointmentBulkRequest request = new
            DeleteAppointmentBulk.DeleteAppointmentBulkRequest(List.of(appointmentId1, appointmentId2, appointmentId3));

        when(appointmentRepository.findAllById(any()))
                .thenReturn(List.of(testAppointment1, testAppointment2, testAppointment3));

        deleteAppointmentBulk.deleteAppointmentsInBulk(request);

        ArgumentCaptor<List<Appointment>> listArgumentCaptor = ArgumentCaptor.captor();
        verify(appointmentRepository).deleteAllInBatch(listArgumentCaptor.capture());

        List<Appointment> deletedAppointments = listArgumentCaptor.getValue();

        assertThat(deletedAppointments).hasSize(3);
        assertThat(deletedAppointments).extracting(Appointment::getId)
                        .containsExactly(appointmentId1, appointmentId2, appointmentId3);
        verify(appointmentRepository).findAllById(request.appointmentIds());

        assertThat(applicationEvents.stream(AppointmentDeletedEvent.class))
                .filteredOn(event -> deletedAppointments.contains(event.getAppointment()))
                .hasSize(deletedAppointments.size());
    }

}
