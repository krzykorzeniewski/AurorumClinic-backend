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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmployeeDeleteSchedule.class})
@ActiveProfiles("test")
public class EmployeeDeleteScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @Autowired
    EmployeeDeleteSchedule employeeDeleteSchedule;

    @Test
    void deleteScheduleShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeDeleteSchedule.deleteSchedule(scheduleId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void deleteScheduleShouldDeleteScheduleWithGivenId() {
        Long scheduleId = 1L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .build();

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        employeeDeleteSchedule.deleteSchedule(scheduleId);

        ArgumentCaptor<Schedule> scheduleArgumentCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleRepository).delete(scheduleArgumentCaptor.capture());

        Schedule deletedSchedule = scheduleArgumentCaptor.getValue();
        assertThat(deletedSchedule).isEqualTo(testSchedule);

        verify(scheduleRepository).findById(scheduleId);
        verify(scheduleValidator).checkIfScheduleHasAppointments(testSchedule);
    }


}
