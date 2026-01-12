package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GetDoctorByIdAppointmentSlotsTest {

    DoctorRepository doctorRepository;
    ServiceRepository serviceRepository;
    AppointmentRepository appointmentRepository;

    GetDoctorByIdAppointmentSlots query;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);

        query = new GetDoctorByIdAppointmentSlots(doctorRepository, serviceRepository, appointmentRepository);
    }

    @SuppressWarnings("unchecked")
    private List<LocalDateTime> invokeHandle(Long doctorId,
                                             LocalDateTime startedAt,
                                             LocalDateTime finishedAt,
                                             Long serviceId) {
        try {
            Method m = GetDoctorByIdAppointmentSlots.class.getDeclaredMethod(
                    "handle", Long.class, LocalDateTime.class, LocalDateTime.class, Long.class
            );
            m.setAccessible(true);
            return (List<LocalDateTime>) m.invoke(query, doctorId, startedAt, finishedAt, serviceId);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldThrowApiNotFoundExceptionWhenDoctorIdNotFound() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invokeHandle(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoMoreInteractions(doctorRepository, serviceRepository, appointmentRepository);
    }

    @Test
    void shouldThrowApiNotFoundExceptionWhenServiceIdNotFound() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of())
                .specializations(Set.of())
                .appointments(List.of())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invokeHandle(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
        verifyNoMoreInteractions(doctorRepository, serviceRepository, appointmentRepository);
    }

    @Test
    void shouldThrowApiExceptionWhenDoctorHasNoSpecializationForService() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .duration(30)
                .build();

        Specialization unrelatedSpec = Specialization.builder()
                .services(Set.of())
                .build();

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of())
                .specializations(Set.of(unrelatedSpec))
                .appointments(List.of())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        assertThatThrownBy(() -> invokeHandle(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiException.class);

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
        verifyNoMoreInteractions(doctorRepository, serviceRepository, appointmentRepository);
    }

    @Test
    void shouldReturnSortedSlotsWithinEffectiveWindowForMatchingScheduleAndService() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime rangeStart = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2026, 1, 1, 11, 0);

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .duration(30)
                .build();

        Schedule schedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2026, 1, 1, 9, 0))
                .finishedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .services(Set.of(serviceFromDb))
                .build();

        Specialization spec = Specialization.builder()
                .services(Set.of(serviceFromDb))
                .build();

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(schedule))
                .specializations(Set.of(spec))
                .appointments(List.of())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));
        when(appointmentRepository.isTimeSlotAvailable(any(), any(), eq(doctorId), eq(serviceId)))
                .thenReturn(true);

        List<LocalDateTime> result = invokeHandle(doctorId, rangeStart, rangeEnd, serviceId);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 30)
        );

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);

        verify(appointmentRepository).isTimeSlotAvailable(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 30),
                doctorId,
                serviceId
        );
        verify(appointmentRepository).isTimeSlotAvailable(
                LocalDateTime.of(2026, 1, 1, 10, 30),
                LocalDateTime.of(2026, 1, 1, 11, 0),
                doctorId,
                serviceId
        );

        verifyNoMoreInteractions(doctorRepository, serviceRepository, appointmentRepository);
    }

    @Test
    void shouldSkipScheduleWhenServiceNotAssignedToSchedule() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime rangeStart = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2026, 1, 1, 12, 0);

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .duration(30)
                .build();

        Service otherService = Service.builder()
                .id(999L)
                .duration(30)
                .build();

        Schedule scheduleWithoutRequestedService = Schedule.builder()
                .startedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .finishedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .services(Set.of(otherService))
                .build();

        Specialization spec = Specialization.builder()
                .services(Set.of(serviceFromDb))
                .build();

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(scheduleWithoutRequestedService))
                .specializations(Set.of(spec))
                .appointments(List.of())
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        List<LocalDateTime> result = invokeHandle(doctorId, rangeStart, rangeEnd, serviceId);

        assertThat(result).isEmpty();

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
        verifyNoInteractions(appointmentRepository);
        verifyNoMoreInteractions(doctorRepository, serviceRepository);
    }

    @Test
    void shouldJumpOverOverlappingAppointmentWhenSlotNotAvailable() {
        Long doctorId = 1L;
        Long serviceId = 5L;

        LocalDateTime rangeStart = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime rangeEnd = LocalDateTime.of(2026, 1, 1, 12, 0);

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .duration(30)
                .build();

        Schedule schedule = Schedule.builder()
                .startedAt(rangeStart)
                .finishedAt(rangeEnd)
                .services(Set.of(serviceFromDb))
                .build();

        Specialization spec = Specialization.builder()
                .services(Set.of(serviceFromDb))
                .build();

        Appointment existing = Appointment.builder()
                .startedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .finishedAt(LocalDateTime.of(2026, 1, 1, 10, 45))
                .build();

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(schedule))
                .specializations(Set.of(spec))
                .appointments(List.of(existing))
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        when(appointmentRepository.isTimeSlotAvailable(any(), any(), eq(doctorId), eq(serviceId)))
                .thenAnswer(inv -> {
                    LocalDateTime slotStart = inv.getArgument(0);
                    return !slotStart.equals(LocalDateTime.of(2026, 1, 1, 10, 0));
                });

        List<LocalDateTime> result = invokeHandle(doctorId, rangeStart, rangeEnd, serviceId);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 1, 1, 10, 45),
                LocalDateTime.of(2026, 1, 1, 11, 15)
        );

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);

        verify(appointmentRepository).isTimeSlotAvailable(
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 30),
                doctorId,
                serviceId
        );
        verify(appointmentRepository).isTimeSlotAvailable(
                LocalDateTime.of(2026, 1, 1, 10, 45),
                LocalDateTime.of(2026, 1, 1, 11, 15),
                doctorId,
                serviceId
        );
        verify(appointmentRepository).isTimeSlotAvailable(
                LocalDateTime.of(2026, 1, 1, 11, 15),
                LocalDateTime.of(2026, 1, 1, 11, 45),
                doctorId,
                serviceId
        );

        verifyNoMoreInteractions(doctorRepository, serviceRepository, appointmentRepository);
    }
}
