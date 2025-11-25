package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DoctorDeleteSchedule.class})
@ActiveProfiles("test")
public class DoctorDeleteScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @Autowired
    DoctorDeleteSchedule doctorDeleteSchedule;

    @Test
    void deleteScheduleShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;
        Long doctorId = 1L;

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDeleteSchedule.deleteSchedule(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void deleteScheduleShouldThrowApiAuthExceptionWhenScheduleDocIdIsNotEqualToRequestDocId() {
        Long scheduleId = 1L;
        Long doctorId = 1L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> doctorDeleteSchedule.deleteSchedule(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void deleteScheduleShouldDeleteScheduleWithGivenId() {
        Long scheduleId = 1L;
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(testDoctor)
                .build();

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        doctorDeleteSchedule.deleteSchedule(scheduleId, doctorId);

        ArgumentCaptor<Schedule> scheduleArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).delete(scheduleArgumentCaptor.capture());

        Schedule deletedSchedule = scheduleArgumentCaptor.getValue();
        assertThat(deletedSchedule).isEqualTo(testSchedule);

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleValidator).checkIfScheduleHasAppointments(testSchedule);
    }

}
