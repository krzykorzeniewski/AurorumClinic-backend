package pl.edu.pja.aurorumclinic.features.appointments.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class AppointmentValidatorTest {

    AppointmentRepository appointmentRepository;
    AppointmentValidator appointmentValidator;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        appointmentValidator = new AppointmentValidator(appointmentRepository);
    }

    @Test
    void validateAppointmentShouldThrowApiExceptionWhenStartDateIsAfterEndDate() {
        LocalDateTime startedAt = LocalDateTime.now().plusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        Service testService = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        assertThatThrownBy(() ->
                appointmentValidator.validateAppointment(startedAt, finishedAt, testDoctor, testService))
                .isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void validateAppointmentShouldThrowApiExceptionWhenStartDateIsInThePast() {
        LocalDateTime startedAt = LocalDateTime.now().minusHours(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        Service testService = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        assertThatThrownBy(() ->
                appointmentValidator.validateAppointment(startedAt, finishedAt, testDoctor, testService))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("past");
    }

    @Test
    void validateAppointmentShouldThrowApiExceptionWhenEndDateIsBeforeStartDate() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().minusMinutes(30);
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        Service testService = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        assertThatThrownBy(() ->
                appointmentValidator.validateAppointment(startedAt, finishedAt, testDoctor, testService))
                .isExactlyInstanceOf(ApiException.class);
    }

    @Test
    void validateAppointmentShouldThrowApiExceptionWhenDoctorSpecializationIsNotAssignedToService() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(Service.builder()
                                .id(1L)
                        .build()))
                .build();
        Service testService = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        assertThatThrownBy(() -> appointmentValidator.validateAppointment(startedAt, finishedAt,
                testDoctor, testService))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("specialization");
    }

    @Test
    void validateAppointmentShouldThrowApiExceptionWhenTimeslotIsNotAvailable() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Service testService = Service.builder()
                .id(1L)
                .build();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(testService))
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        when(appointmentRepository.isTimeSlotAvailable(any(), any(), anyLong(), anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> appointmentValidator.validateAppointment(startedAt, finishedAt,
                testDoctor, testService))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("timeslot");
        verify(appointmentRepository).isTimeSlotAvailable(startedAt, finishedAt, testDoctor.getId(), testService.getId());
    }

    @Test
    void validateAppointmentShouldReturnNothingWhenAppointmentDataIsCorrect() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Service testService = Service.builder()
                .id(1L)
                .build();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(testService))
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();

        when(appointmentRepository.isTimeSlotAvailable(any(), any(), anyLong(), anyLong()))
                .thenReturn(true);

        assertThatNoException().isThrownBy(() ->
                appointmentValidator.validateAppointment(startedAt, finishedAt, testDoctor, testService));
        verify(appointmentRepository).isTimeSlotAvailable(startedAt, finishedAt, testDoctor.getId(), testService.getId());
    }

    @Test
    void validateRescheduledAppointmentShouldThrowApiExceptionWhenDoctorSpecializationIsNotAssignedToService() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        Service testService = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .doctor(testDoctor)
                .service(testService)
                .build();

        assertThatThrownBy(() -> appointmentValidator.validateRescheduledAppointment(startedAt, finishedAt,
                testDoctor, testService, testAppointment))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("specialization");
    }

    @Test
    void validateRescheduledAppointmentShouldThrowApiExceptionWhenTimeslotIsNotAvailable() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Service testService = Service.builder()
                .id(1L)
                .build();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(testService))
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .doctor(testDoctor)
                .service(testService)
                .build();

        when(appointmentRepository.isTimeSlotAvailableExcludingId(any(), any(), anyLong(), anyLong(), anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> appointmentValidator.validateRescheduledAppointment(startedAt, finishedAt,
                testDoctor, testService, testAppointment))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("timeslot");
        verify(appointmentRepository).isTimeSlotAvailableExcludingId(startedAt, finishedAt, testDoctor.getId(),
                testService.getId(), testAppointment.getId());
    }

    @Test
    void validateRescheduledAppointmentShouldReturnNothingWhenAppointmentDataIsCorrect() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now();
        Service testService = Service.builder()
                .id(1L)
                .build();
        Specialization testSpec = Specialization.builder()
                .id(1L)
                .name("Psychiatra dorosłych")
                .services(Set.of(testService))
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(testSpec))
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .doctor(testDoctor)
                .service(testService)
                .build();

        when(appointmentRepository.isTimeSlotAvailableExcludingId(any(), any(), anyLong(), anyLong(), anyLong()))
                .thenReturn(true);

        assertThatNoException().isThrownBy(() -> appointmentValidator.validateRescheduledAppointment(startedAt, finishedAt,
                testDoctor, testService, testAppointment));
        verify(appointmentRepository).isTimeSlotAvailableExcludingId(startedAt, finishedAt, testDoctor.getId(),
                testService.getId(), testAppointment.getId());
    }
}
