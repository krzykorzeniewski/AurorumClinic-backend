package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

}
