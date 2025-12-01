package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class GetDoctorByIdAppointmentSlotsTest {

    DoctorRepository doctorRepository;

    ServiceRepository serviceRepository;

    AppointmentRepository appointmentRepository;

    GetDoctorByIdAppointmentSlots getDoctorByIdAppointmentSlots;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        getDoctorByIdAppointmentSlots =
                new GetDoctorByIdAppointmentSlots(doctorRepository, serviceRepository, appointmentRepository);
    }

    @Test
    void getAppointmentSlotsShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 1L;
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(5);
        Long serviceId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                getDoctorByIdAppointmentSlots.getAppointmentSlots(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("doctor");
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void getAppointmentSlotsShouldThrowApiNotFoundExceptionWhenServiceIdIsNotFound() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .startedAt(LocalDateTime.now())
                                .finishedAt(LocalDateTime.now().plusHours(8))
                                .build()
                ))
                .build();
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(5);
        Long serviceId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                getDoctorByIdAppointmentSlots.getAppointmentSlots(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("service");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void getAppointmentSlotsShouldThrowApiExceptionWhenDoctorSpecializationIsNotAssignedToService() {
        Long serviceId = 2L;
        Service testService = Service.builder()
                .id(serviceId)
                .specializations(new HashSet<>())
                .build();

        Specialization testSpecialization = Specialization.builder()
                .id(2147L)
                .services(new HashSet<>())
                .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .startedAt(LocalDateTime.now())
                                .finishedAt(LocalDateTime.now().plusHours(8))
                                .build()
                ))
                .specializations((
                        Set.of(testSpecialization)
                ))
                .build();
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(5);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(testService));

        assertThatThrownBy(() ->
                getDoctorByIdAppointmentSlots.getAppointmentSlots(doctorId, startedAt, finishedAt, serviceId))
                .isExactlyInstanceOf(ApiException.class);
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void getAppointmentSlotsShouldReturnListOfAvailableDateTimesBetweenDatesWithinScheduleForDoctorIdAndServiceId() {
        Long serviceId = 2L;
        Service testService = Service.builder()
                .id(serviceId)
                .duration(30)
                .specializations(new HashSet<>())
                .build();
        Specialization testSpecialization = Specialization.builder()
                .id(2147L)
                .services(new HashSet<>())
                .build();
        testService.getSpecializations().add(testSpecialization);
        testSpecialization.getServices().add(testService);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .startedAt(LocalDateTime.of(2025, 12, 1, 8, 0))
                                .finishedAt(LocalDateTime.of(2025, 12, 1, 17, 0))
                                .services(Set.of(testService))
                                .build()
                ))
                .specializations((
                        Set.of(testSpecialization)
                ))
                .build();
        LocalDateTime startedAt = LocalDateTime.of(2025, 12, 1, 9, 0);
        LocalDateTime finishedAt = startedAt.plusHours(3);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(testService));
        when(appointmentRepository.isTimeSlotAvailable(any(), any(), anyLong(), anyLong())).thenReturn(true);

        ResponseEntity<ApiResponse<List<LocalDateTime>>> responseEntity
                = getDoctorByIdAppointmentSlots.getAppointmentSlots(doctorId, startedAt, finishedAt, serviceId);

        assertThat(responseEntity.getBody()).isNotNull();

        List<LocalDateTime> resultList = responseEntity.getBody().getData();

        assertThat(resultList).isNotEmpty();
        assertThat(resultList).hasSize(6);
        assertThat(resultList).containsExactly(LocalDateTime.of(2025, 12, 1, 9, 0),
                LocalDateTime.of(2025, 12, 1, 9, 30),
                LocalDateTime.of(2025, 12, 1, 10, 0),
                LocalDateTime.of(2025, 12, 1, 10, 30),
                LocalDateTime.of(2025, 12, 1, 11, 0),
                LocalDateTime.of(2025, 12, 1, 11, 30));

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
        verify(appointmentRepository, times(6))
                .isTimeSlotAvailable(any(), any(), anyLong(), anyLong());
    }

    @Test
    void getAppointmentSlotsShouldReturnListOfAvailableDateTimesBetweenDatesWithinScheduleForDoctorIdAndServiceIdAndSkipExistingAppointments() {
        Long serviceId = 2L;
        Service testService = Service.builder()
                .id(serviceId)
                .duration(30)
                .specializations(new HashSet<>())
                .build();
        Specialization testSpecialization = Specialization.builder()
                .id(2147L)
                .services(new HashSet<>())
                .build();
        testService.getSpecializations().add(testSpecialization);
        testSpecialization.getServices().add(testService);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .startedAt(LocalDateTime.of(2025, 12, 1, 9, 0))
                                .finishedAt(LocalDateTime.of(2025, 12, 1, 12, 0))
                                .services(Set.of(testService))
                                .build()
                ))
                .specializations((
                        Set.of(testSpecialization)
                ))
                .appointments(new ArrayList<>())
                .build();
        Appointment testAppointment = Appointment.builder()
                .doctor(testDoctor)
                .startedAt(LocalDateTime.of(2025, 12, 1, 10, 0))
                .finishedAt(LocalDateTime.of(2025, 12, 1, 10, 45))
                .build();
        testDoctor.getAppointments().add(testAppointment);
        LocalDateTime startedAt = LocalDateTime.of(2025, 12, 1, 9, 0);
        LocalDateTime finishedAt = startedAt.plusHours(10);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findById(anyLong())).thenReturn(Optional.of(testService));
        when(appointmentRepository.isTimeSlotAvailable(eq(startedAt), eq(startedAt.plusMinutes(30)),
                anyLong(), anyLong())).thenReturn(false);

        ResponseEntity<ApiResponse<List<LocalDateTime>>> responseEntity
                = getDoctorByIdAppointmentSlots.getAppointmentSlots(doctorId, startedAt, finishedAt, serviceId);

        assertThat(responseEntity.getBody()).isNotNull();

        List<LocalDateTime> resultList = responseEntity.getBody().getData();

        assertThat(resultList).isNotEmpty();
        assertThat(resultList).hasSize(4);
        assertThat(resultList).containsExactly(LocalDateTime.of(2025, 12, 1, 9, 0),
                LocalDateTime.of(2025, 12, 1, 9, 30),
                LocalDateTime.of(2025, 12, 1, 10, 45),
                LocalDateTime.of(2025, 12, 1, 11, 15));

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findById(serviceId);
        verify(appointmentRepository, times(5))
                .isTimeSlotAvailable(any(), any(), anyLong(), anyLong());
    }

}
