package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DoctorUpdateSchedule.class})
@ActiveProfiles("test")
public class DoctorUpdateScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    DoctorUpdateSchedule doctorUpdateSchedule;

    @Test
    void updateScheduleShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;
        Long doctorId = 1L;
        DoctorUpdateSchedule.DocUpdateScheduleRequest request = new DoctorUpdateSchedule.DocUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), new HashSet<>()
        );

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorUpdateSchedule.updateSchedule(scheduleId, request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void updateScheduleShouldThrowApiAuthExceptionWhenScheduleDocIdIsNotEqualRequestDocId() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        Long scheduleId = 1L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();

        DoctorUpdateSchedule.DocUpdateScheduleRequest request = new DoctorUpdateSchedule.DocUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), new HashSet<>()
        );

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> doctorUpdateSchedule.updateSchedule(scheduleId, request, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(scheduleRepository).findById(scheduleId);
    }



    @Test
    void updateScheduleShouldThrowApiExceptionWhenServiceIdIsNotFound() { //TODO
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        Long scheduleId = 1L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();

        DoctorUpdateSchedule.DocUpdateScheduleRequest request = new DoctorUpdateSchedule.DocUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), new HashSet<>()
        );

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> doctorUpdateSchedule.updateSchedule(scheduleId, request, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(scheduleRepository).findById(scheduleId);
    }
}
