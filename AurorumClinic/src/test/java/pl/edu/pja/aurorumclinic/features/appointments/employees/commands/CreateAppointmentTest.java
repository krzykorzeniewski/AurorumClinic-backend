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
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CreateAppointment.class})
@ActiveProfiles("test")
@RecordApplicationEvents
public class CreateAppointmentTest {

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
    CreateAppointment createAppointment;

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenPatientIdIsNotFound() {
        CreateAppointment.EmployeeCreateAppointmentRequest request =
                new CreateAppointment.EmployeeCreateAppointmentRequest(1L, LocalDateTime.now(), 1L,
                        1L, "opis");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createAppointment.createAppointment(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(patientRepository).findById(request.patientId());
    }

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        CreateAppointment.EmployeeCreateAppointmentRequest request =
                new CreateAppointment.EmployeeCreateAppointmentRequest(1L, LocalDateTime.now(), 1L,
                        1L, "opis");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(Patient.builder()
                        .id(request.patientId())
                .build()));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createAppointment.createAppointment(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(patientRepository).findById(request.patientId());
        verify(doctorRepository).findById(request.doctorId());
    }

    @Test
    void createAppointmentShouldThrowApiNotFoundExceptionWhenServiceIdIsNotFound() {
        CreateAppointment.EmployeeCreateAppointmentRequest request =
                new CreateAppointment.EmployeeCreateAppointmentRequest(1L, LocalDateTime.now(), 1L,
                        1L, "opis");

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(Patient.builder()
                .id(request.patientId())
                .build()));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(Doctor.builder()
                        .id(request.doctorId())
                .build()));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createAppointment.createAppointment(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(patientRepository).findById(request.patientId());
        verify(doctorRepository).findById(request.doctorId());
        verify(serviceRepository).findById(request.serviceId());
    }

    @Test
    void createAppointmentShouldSaveNewAppointmentAndPublishEvent(
            @Autowired ApplicationEvents applicationEvents
            ) {
        CreateAppointment.EmployeeCreateAppointmentRequest request =
                new CreateAppointment.EmployeeCreateAppointmentRequest(1L, LocalDateTime.now(), 1L,
                        1L, "opis");
        Patient testPatient = Patient.builder()
                .id(request.patientId())
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

        createAppointment.createAppointment(request);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

        Appointment savedAppointment = appointmentArgumentCaptor.getValue();
        assertThat(savedAppointment).isNotNull();
        assertThat(savedAppointment.getService()).isEqualTo(testService);
        assertThat(savedAppointment.getDescription()).isEqualTo(request.description());
        assertThat(savedAppointment.getPatient()).isEqualTo(testPatient);
        assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.CREATED);
        assertThat(savedAppointment.getDoctor()).isEqualTo(testDoctor);
        assertThat(savedAppointment.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(savedAppointment.getFinishedAt()).isEqualTo(finishedAt);

        verify(appointmentValidator).validateAppointment(savedAppointment.getStartedAt(), savedAppointment.getFinishedAt(),
                savedAppointment.getDoctor(), savedAppointment.getService());

        assertThat(applicationEvents.stream(AppointmentCreatedEvent.class))
                .filteredOn(event ->
                        Objects.equals(event.getAppointment(), savedAppointment)
                                && Objects.equals(event.getPatient(), testPatient))
                .hasSize(1);
    }

}
