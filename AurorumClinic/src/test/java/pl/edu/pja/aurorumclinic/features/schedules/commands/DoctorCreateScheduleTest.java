package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DoctorCreateSchedule.class})
@ActiveProfiles("test")
public class DoctorCreateScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ServiceRepository serviceRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @Autowired
    DoctorCreateSchedule doctorCreateSchedule;

    @Test
    void createScheduleShouldThrowApiExceptionWhenServiceIdIsNotFound() {
        Set<Long> serviceIds = Set.of(1L, 2L, 3L, 4L);
        DoctorCreateSchedule.CreateScheduleRequest request =
                new DoctorCreateSchedule.CreateScheduleRequest(
                        LocalDateTime.now(), LocalDateTime.now().plusHours(12), serviceIds);
        Long doctorId = 1L;

        when(serviceRepository.findAllById(any())).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> doctorCreateSchedule.createSchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class);
        verify(serviceRepository).findAllById(request.serviceIds());
    }

    @Test
    void createScheduleShouldThrowApiExceptionWhenDoctorIdIsNotFound() {
        Service testService = Service.builder()
                .id(1L)
                .build();
        Set<Long> serviceIds = Set.of(testService.getId());
        DoctorCreateSchedule.CreateScheduleRequest request =
                new DoctorCreateSchedule.CreateScheduleRequest(
                        LocalDateTime.now(), LocalDateTime.now().plusHours(12), serviceIds);
        Long doctorId = 1L;

        when(serviceRepository.findAllById(any())).thenReturn(List.of(testService));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorCreateSchedule.createSchedule(request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(serviceRepository).findAllById(request.serviceIds());
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void createScheduleShouldSaveSchedule() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Service testService = Service.builder()
                .id(1L)
                .build();
        Set<Long> serviceIds = Set.of(testService.getId());
        DoctorCreateSchedule.CreateScheduleRequest request =
                new DoctorCreateSchedule.CreateScheduleRequest(
                        LocalDateTime.now(), LocalDateTime.now().plusHours(12), serviceIds);
        List<Service> servicesToReturn = List.of(testService);

        when(serviceRepository.findAllById(any())).thenReturn(servicesToReturn);
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        doctorCreateSchedule.createSchedule(request, doctorId);

        ArgumentCaptor<Schedule> scheduleArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).save(scheduleArgumentCaptor.capture());

        Schedule savedSchedule = scheduleArgumentCaptor.getValue();
        assertThat(savedSchedule.getDoctor()).isEqualTo(testDoctor);
        assertThat(savedSchedule.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(savedSchedule.getFinishedAt()).isEqualTo(request.finishedAt());
        assertThat(savedSchedule.getServices()).isEqualTo(new HashSet<>(servicesToReturn));

        verify(scheduleValidator).validateTimeslotAndServices(request.startedAt(), request.finishedAt(), testDoctor,
                servicesToReturn);
        verify(serviceRepository).findAllById(request.serviceIds());
        verify(doctorRepository).findById(doctorId);
    }
}
