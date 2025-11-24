package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PatientCreateAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class PatientCreateAppointmentTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    PatientRepository patientRepository;

    @MockitoBean
    ServiceRepository serviceRepository;

    @MockitoBean
    AppointmentValidator appointmentValidator;

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @Autowired
    PatientCreateAppointment patientCreateAppointment;

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenPatientIdIsNotFound() {
        PatientCreateAppointment.PatientCreateAppointmentRequest request = new
                PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now(), 1L, 1L, "text");
        Long patientId = 1L;

        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientCreateAppointment.createAppointment(request, patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("not found");
        verify(patientRepository).findById(patientId);
    }

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        PatientCreateAppointment.PatientCreateAppointmentRequest request = new
                PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now(), 1L, 1L, "text");
        Long patientId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientCreateAppointment.createAppointment(request, patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("not found");
        verify(doctorRepository).findById(request.doctorId());
        verify(patientRepository).findById(patientId);
    }

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenServiceIdIsNotFound() {
        PatientCreateAppointment.PatientCreateAppointmentRequest request = new
                PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now(), 1L, 1L, "text");
        Long patientId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(request.doctorId())
                .build();

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientCreateAppointment.createAppointment(request, patientId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("not found");
        verify(doctorRepository).findById(request.doctorId());
        verify(patientRepository).findById(patientId);
        verify(serviceRepository).findById(request.serviceId());
    }

    @Test
    void createAppointmentShouldSaveAppointmentAndPublishEventWhenDataIsCorrect(
            @Autowired ApplicationEvents applicationEvents
            ) {
        PatientCreateAppointment.PatientCreateAppointmentRequest request = new
                PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now(), 1L, 1L, "text");
        Long patientId = 1L;
        Patient testPatient = Patient.builder()
                .id(patientId)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(request.doctorId())
                .build();
        Service testService = Service.builder()
                .id(request.serviceId())
                .duration(30)
                .build();
        LocalDateTime finishedAt = request.startedAt().plusMinutes(testService.getDuration());

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(testService));

        patientCreateAppointment.createAppointment(request, patientId);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

        Appointment savedAppointment = appointmentArgumentCaptor.getValue();

        assertThat(savedAppointment.getService()).isEqualTo(testService);
        assertThat(savedAppointment.getDoctor()).isEqualTo(testDoctor);
        assertThat(savedAppointment.getPatient()).isEqualTo(testPatient);
        assertThat(savedAppointment.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(savedAppointment.getDescription()).isEqualTo(request.description());

        verify(appointmentValidator).validateAppointment(request.startedAt(), finishedAt, testDoctor, testService);
        assertThat(applicationEvents.stream(AppointmentCreatedEvent.class)
                ).filteredOn((event ->
                        Objects.equals(event.getPatient(), testPatient)
                        && Objects.equals(event.getAppointment(), savedAppointment)))
                .hasSize(1);
    }

}
