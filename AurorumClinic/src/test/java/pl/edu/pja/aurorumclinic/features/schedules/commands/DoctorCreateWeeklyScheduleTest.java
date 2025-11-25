package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DoctorCreateWeeklySchedule.class})
@ActiveProfiles("test")
public class DoctorCreateWeeklyScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ServiceRepository serviceRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @Autowired
    DoctorCreateWeeklySchedule doctorCreateWeeklySchedule;

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenFinishedAtIsBeforeStartedAt() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
            DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                    .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                            .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                            .serviceIds(Set.of(1L, 2L))
                            .build())
                    .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                            .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                            .serviceIds(Set.of(3L))
                            .build())
                    .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                            .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                            .serviceIds(Set.of(1L, 2L))
                            .build())
                    .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                            .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                            .serviceIds(Set.of(3L))
                            .build())
                    .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                            .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                            .serviceIds(Set.of(1L, 2L))
                            .build())
                    .startedAt(LocalDate.now())
                    .finishedAt(LocalDate.now().minusDays(1)) //before startedAt
                    .build();
        Long doctorId = 1L;

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("is before started");
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenStartedAtAndFinishedAtAreInThePast() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .startedAt(LocalDate.now().minusDays(10))
                        .finishedAt(LocalDate.now().minusDays(1))
                        .build();
        Long doctorId = 1L;

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("past");
    }

    @Test
    void createWeeklyScheduleShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenMonServiceIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findAllById(any())).thenReturn((List.of(Service.builder().id(1L).build()))); //mon has 1L and 2L

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Monday");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenTueServiceIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())); //tue has 3L

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Tuesday");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenWedServiceIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(4L).build())); //wed has 1L, 3L

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Wednesday");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenThuServiceIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(5L).build())); //thu has 4L

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Thursday");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenFriServiceIdIsNotFound() {
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(LocalDate.now())
                        .finishedAt(LocalDate.now().plusMonths(6))
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(4L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build())); //fri has 1L, 4L

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Friday");
        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
        verify(serviceRepository).findAllById(request.fri().serviceIds());
    }

    @Test
    void createWeeklyScheduleShouldSaveAllDaysWithinWeeklySchedule() {
        LocalDate scheduleStart = LocalDate.of(2025, 11, 24); //monday
        LocalDate scheduleFinish = LocalDate.of(2025, 12, 7); //sunday
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(scheduleStart)
                        .finishedAt(scheduleFinish)
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(4L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(4L).build()));

        MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(scheduleStart);

        doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId);

        ArgumentCaptor<Schedule> schedulesArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);

        verify(scheduleRepository, times(10)).save(schedulesArgumentCaptor.capture());

        List<Schedule> savedSchedules = schedulesArgumentCaptor.getAllValues();

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SUNDAY)
                || Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SATURDAY))
                .hasSize(0);

        assertThat(savedSchedules).filteredOn(
                schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.MONDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.mon().hours().get(0), request.mon().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.TUESDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.tue().hours().get(0), request.tue().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.WEDNESDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.wed().hours().get(0), request.wed().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.THURSDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.thu().hours().get(0), request.thu().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.FRIDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.fri().hours().get(0), request.fri().hours().get(1)));

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
        verify(serviceRepository).findAllById(request.fri().serviceIds());
        verify(scheduleValidator, times(10))
                .validateTimeslotAndServices(any(), any(), any(), any());

        mockedLocalDate.close();
    }

    @Test
    void createWeeklyScheduleShouldSkipDayIfAbsenceExistsWithin() {
        LocalDate scheduleStart = LocalDate.of(2025, 11, 24); //monday
        LocalDate scheduleFinish = LocalDate.of(2025, 12, 7); //sunday
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(scheduleStart)
                        .finishedAt(scheduleFinish)
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(4L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(4L).build()));

        doThrow(new ApiException("Schedule overlaps with already existing absence", "absence"))
                .when(scheduleValidator).validateTimeslotAndServices(
                        eq(LocalDateTime.of(scheduleStart, request.mon().hours().get(0))), any(), any(), any()); //first monday absence

        MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(scheduleStart);

        doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId);

        ArgumentCaptor<Schedule> schedulesArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);

        verify(scheduleRepository, times(9)).save(schedulesArgumentCaptor.capture());

        List<Schedule> savedSchedules = schedulesArgumentCaptor.getAllValues();

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SUNDAY)
                                || Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SATURDAY))
                .hasSize(0);

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.MONDAY))
                .hasSize(1)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.mon().hours().get(0), request.mon().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.TUESDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.tue().hours().get(0), request.tue().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.WEDNESDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.wed().hours().get(0), request.wed().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.THURSDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.thu().hours().get(0), request.thu().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.FRIDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.fri().hours().get(0), request.fri().hours().get(1)));

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
        verify(serviceRepository).findAllById(request.fri().serviceIds());
        verify(scheduleValidator, times(10))
                .validateTimeslotAndServices(any(), any(), any(), any());

        mockedLocalDate.close();
    }

    @Test
    void createWeeklyScheduleShouldNotSavePastScheduleDaysIfStartedAtIsInThePast() {
        LocalDate scheduleStart = LocalDate.of(2025, 11, 24); //monday
        LocalDate scheduleFinish = LocalDate.of(2025, 12, 7); //sunday
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(scheduleStart)
                        .finishedAt(scheduleFinish)
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(4L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(4L).build()));

        MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(scheduleStart.plusDays(2)); //skip monday and tuesday

        doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId);

        ArgumentCaptor<Schedule> schedulesArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);

        verify(scheduleRepository, times(8)).save(schedulesArgumentCaptor.capture());

        List<Schedule> savedSchedules = schedulesArgumentCaptor.getAllValues();

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SUNDAY)
                                || Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.SATURDAY))
                .hasSize(0);

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.MONDAY))
                .hasSize(1)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.mon().hours().get(0), request.mon().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.TUESDAY))
                .hasSize(1)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.tue().hours().get(0), request.tue().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.WEDNESDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.wed().hours().get(0), request.wed().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.THURSDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.thu().hours().get(0), request.thu().hours().get(1)));

        assertThat(savedSchedules).filteredOn(
                        schedule -> Objects.equals(schedule.getStartedAt().getDayOfWeek(), DayOfWeek.FRIDAY))
                .hasSize(2)
                .extracting(schedule ->
                        Set.of(schedule.getStartedAt().toLocalTime(), schedule.getFinishedAt().toLocalTime()))
                .containsOnly(Set.of(request.fri().hours().get(0), request.fri().hours().get(1)));

        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
        verify(serviceRepository).findAllById(request.fri().serviceIds());
        verify(scheduleValidator, times(8))
                .validateTimeslotAndServices(any(), any(), any(), any());

        mockedLocalDate.close();
    }

    @Test
    void createWeeklyScheduleShouldThrowApiExceptionWhenScheduleValidationFails() {
        LocalDate scheduleStart = LocalDate.of(2025, 11, 24); //monday
        LocalDate scheduleFinish = LocalDate.of(2025, 12, 7); //sunday
        DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request =
                DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.builder()
                        .mon(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 2L))
                                .build())
                        .tue(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(3L))
                                .build())
                        .wed(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 3L))
                                .build())
                        .thu(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                                .serviceIds(Set.of(4L))
                                .build())
                        .fri(DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest.DayDto.builder()
                                .hours(List.of(LocalTime.of(8, 0), LocalTime.of(17, 0)))
                                .serviceIds(Set.of(1L, 4L))
                                .build())
                        .startedAt(scheduleStart)
                        .finishedAt(scheduleFinish)
                        .build();
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        when(serviceRepository.findAllById(any()))
                .thenReturn((List.of(Service.builder().id(1L).build(), Service.builder().id(2L).build())))
                .thenReturn(List.of(Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(3L).build()))
                .thenReturn(List.of(Service.builder().id(4L).build()))
                .thenReturn(List.of(Service.builder().id(1L).build(), Service.builder().id(4L).build()));

        doThrow(new ApiException("Schedule overlaps with already existing schedule", "schedule"))
                .when(scheduleValidator).validateTimeslotAndServices(
                        eq(LocalDateTime.of(scheduleStart, request.mon().hours().get(0))), any(), any(), any()); //first monday validation failed

        MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        mockedLocalDate.when(LocalDate::now).thenReturn(scheduleStart);

        assertThatThrownBy(() -> doctorCreateWeeklySchedule.createWeeklySchedule(request, doctorId))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Schedule overlaps with already existing schedule");


        verify(doctorRepository).findById(doctorId);
        verify(serviceRepository).findAllById(request.mon().serviceIds());
        verify(serviceRepository).findAllById(request.tue().serviceIds());
        verify(serviceRepository).findAllById(request.wed().serviceIds());
        verify(serviceRepository).findAllById(request.thu().serviceIds());
        verify(serviceRepository).findAllById(request.fri().serviceIds());
        verify(scheduleValidator, times(1))
                .validateTimeslotAndServices(any(), any(), any(), any());

        mockedLocalDate.close();
    }

}
