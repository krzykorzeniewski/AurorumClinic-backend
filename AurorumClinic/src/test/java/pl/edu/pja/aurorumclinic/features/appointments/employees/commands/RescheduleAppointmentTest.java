package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

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
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {RescheduleAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class RescheduleAppointmentTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    AppointmentValidator appointmentValidator;

    @Autowired
    RescheduleAppointment rescheduleAppointment;

    @Test
    void updateAppointmentShouldThrowApiNotFoundExceptionWhenAppointmentIdNotFound() {
        Long appointmentId = 1L;
        RescheduleAppointment.EmployeeUpdateAppointmentRequest request =
                new RescheduleAppointment.EmployeeUpdateAppointmentRequest(LocalDateTime.now(), "opis");

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                rescheduleAppointment.updateAppointment(request, appointmentId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void updateAppointmentShouldUpdateAppointmentFieldsAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
            ) {
        Long appointmentId = 1L;
        RescheduleAppointment.EmployeeUpdateAppointmentRequest request =
                new RescheduleAppointment.EmployeeUpdateAppointmentRequest(LocalDateTime.now().plusDays(10), "opis");
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .startedAt(LocalDateTime.now().plusDays(5))
                .description("inny opis")
                .finishedAt(LocalDateTime.now().plusDays(5).plusMinutes(30))
                .service(Service.builder()
                        .id(1L)
                        .duration(30)
                        .build())
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .patient(Patient.builder()
                        .id(1L)
                        .build())
                .build();
        LocalDateTime newFinishedAt = request.startedAt().plusMinutes(testAppointment.getService().getDuration());

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        rescheduleAppointment.updateAppointment(request, appointmentId);

        assertThat(testAppointment.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(testAppointment.getFinishedAt()).isEqualTo(newFinishedAt);
        assertThat(testAppointment.getDescription()).isEqualTo(request.description());
        verify(appointmentValidator).validateRescheduledAppointment(request.startedAt(), newFinishedAt,
                testAppointment.getDoctor(), testAppointment.getService(), testAppointment);

        assertThat(applicationEvents.stream(AppointmentRescheduledEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.getAppointment(), testAppointment)
                        && Objects.equals(event.getPatient(), testAppointment.getPatient()))
                .hasSize(1);
    }

}
