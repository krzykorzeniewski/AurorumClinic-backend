package pl.edu.pja.aurorumclinic.features.schedules.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EmployeeUpdateSchedule.class})
@ActiveProfiles("test")
public class EmployeeUpdateScheduleTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ScheduleValidator scheduleValidator;

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    EmployeeUpdateSchedule employeeUpdateSchedule;

    @Test
    void updateScheduleShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;
        EmployeeUpdateSchedule.EmpUpdateScheduleRequest request =
                new EmployeeUpdateSchedule.EmpUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), new HashSet<>()
        );

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeUpdateSchedule.updateSchedule(scheduleId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void updateScheduleShouldThrowApiExceptionWhenServiceIdIsNotFound() {
        Long scheduleId = 1L;
        Set<Long> serviceIds = Set.of(1L, 2L, 3L);
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .build();
        Service testService = Service.builder()
                .id(1L)
                .build();
        List<Service> returnedServices = List.of(testService);

        EmployeeUpdateSchedule.EmpUpdateScheduleRequest request = new EmployeeUpdateSchedule.EmpUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), serviceIds);

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));
        when(serviceRepository.findAllById(any())).thenReturn(returnedServices);

        assertThatThrownBy(() -> employeeUpdateSchedule.updateSchedule(scheduleId, request))
                .isExactlyInstanceOf(ApiException.class);
        verify(scheduleRepository).findById(scheduleId);
        verify(serviceRepository).findAllById(request.serviceIds());
    }

    @Test
    void updateScheduleShouldUpdateScheduleFieldsWithValuesFromDto() {
        Long scheduleId = 1L;
        Set<Long> serviceIds = Set.of(1L, 2L);
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .build();
        Service testService1 = Service.builder()
                .id(1L)
                .build();
        Service testService2 = Service.builder()
                .id(2L)
                .build();
        List<Service> returnedServices = List.of(testService1, testService2);

        EmployeeUpdateSchedule.EmpUpdateScheduleRequest request = new EmployeeUpdateSchedule.EmpUpdateScheduleRequest(
                LocalDateTime.now(), LocalDateTime.now().plusHours(12), serviceIds);

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));
        when(serviceRepository.findAllById(any())).thenReturn(returnedServices);

        employeeUpdateSchedule.updateSchedule(scheduleId, request);
        assertThat(testSchedule.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(testSchedule.getFinishedAt()).isEqualTo(request.finishedAt());
        assertThat(testSchedule.getServices()).isEqualTo(new HashSet<>(returnedServices));

        verify(scheduleRepository).findById(scheduleId);
        verify(serviceRepository).findAllById(request.serviceIds());
        verify(scheduleValidator).validateNewTimeslotAndServices(request.startedAt(), request.finishedAt(),
                testSchedule.getDoctor(), returnedServices, testSchedule);
        verify(scheduleValidator)
                .checkIfScheduleHasAppointmentsInOldTimeslot(testSchedule, request.startedAt(), request.finishedAt());
    }

}
