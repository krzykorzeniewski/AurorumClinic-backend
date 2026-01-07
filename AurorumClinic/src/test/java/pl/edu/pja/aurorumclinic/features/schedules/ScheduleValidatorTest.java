package pl.edu.pja.aurorumclinic.features.schedules;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = {ScheduleValidator.class})
@ActiveProfiles("test")
public class ScheduleValidatorTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    AbsenceRepository absenceRepository;

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    ScheduleValidator scheduleValidator;

    @Value("${workday.end.hour}")
    Integer endOfDay;

    @Value("${workday.start.hour}")
    Integer startOfDay;

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleIsAtWeekend() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 15, 10))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("weekend");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleIsShorterThenMinServiceDuration() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 8, 10))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("minimum service duration");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleIsLongerThanOneDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 23, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("one day");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleStartedAtIsBeforeStartOfWorkDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, startOfDay - 1, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("before work hours");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleFinishedAtIsAfterEndOfWorkDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 10, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, endOfDay + 1, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("after work hours");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleStartedAtIsAfterFinishedAt() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 10, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("end date");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleFinishedAtIsBeforeStartedAt() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 10, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("end date");
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenScheduleExistsWithinTimeslot() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 18, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("Schedule overlaps")
                .doesNotContainIgnoringCase("absence");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenAbsenceExistsWithinTimeslot() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 18, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("absence");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
    }

    @Test
    void validateTimeslotAndServicesShouldThrowApiExceptionWhenDoctorSpecializationIsNotAssignedToService() {
        Service testService1 = Service.builder()
                .id(1L)
                .build();
        Service testService2 = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .services(Set.of(testService1))
                                .build()
                ))
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 18, 0))
                .doctor(testDoctor)
                .services(Set.of(testService2))
                .build();

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("specialization");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
    }

    @Test
    void validateTimeslotAndServicesShouldNotThrowApiExceptionWhenDoctorSpecializationIsAssignedToService() {
        Service testService1 = Service.builder()
                .id(1L)
                .build();
        Service testService2 = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .services(Set.of(testService2, testService1))
                                .build()
                ))
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 18, 0))
                .doctor(testDoctor)
                .services(Set.of(testService2))
                .build();

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);

        assertThatNoException().isThrownBy(() -> scheduleValidator.validateTimeslotAndServices(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor(), List.copyOf(testSchedule.getServices())));
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(testSchedule.getStartedAt(),
                testSchedule.getFinishedAt(), testSchedule.getDoctor().getId());
    }

    @Test
    void validateNewTimeSlotAndServicesShouldThrowApiExceptionWhenNewScheduleIsLongerThanOneDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 20, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 20, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 22, 8, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 23, 21, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("one day");
    }

    @Test
    void validateNewTimeSlotAndServicesShouldThrowApiExceptionWhenNewScheduleStartedAtIsBeforeStartOfWorkDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, startOfDay - 1, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 16, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("before work hours");
    }

    @Test
    void validateNewTimeSlotAndServicesShouldThrowApiExceptionWhenNewScheduleFinishedAtIsAfterEndOfWorkDay() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 8, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, endOfDay + 1, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("after work hours");
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenNewScheduleStartedAtIsAfterNewFinishedAt() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 10, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 9, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("end date");
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenNewScheduleIsShorterThanMinimunServiceDuration() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 10, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 10, 10);

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("minimum service duration");
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenNewScheduleFinishedAtIsBeforeNewStartedAt() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 10, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 9, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(new Service()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("end date");
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenOtherScheduleExistsInNewTimeslot() {
        Service testService = Service.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .id(100L)
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(testService))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 9, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 18, 0);

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(testService), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing one");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctorExcludingId(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId(), testSchedule.getId());
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenAbsenceExistsInNewTimeslot() {
        Service testService = Service.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .id(100L)
                .startedAt(LocalDateTime.of(2025, 11, 22, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 21, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(testService))
                .build();
        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 9, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 18, 0);

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(testService), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing absence");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctorExcludingId(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId(), testSchedule.getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId());
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenDoctorSpecializationIsNotAssignedToService() {
        Service testService1 = Service.builder()
                .id(1L)
                .build();
        Service testService2 = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .services(Set.of(testService1))
                                .build()
                ))
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .services(Set.of(testService2))
                .build();

        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 9, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 18, 0);

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(testService2), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("specialization");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctorExcludingId(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId(), testSchedule.getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId());
    }

    @Test
    void validateNewTimeslotAndServicesShouldNotThrowApiExceptionWhenDoctorSpecializationIsAssignedToService() {
        Service testService1 = Service.builder()
                .id(1L)
                .build();
        Service testService2 = Service.builder()
                .id(2L)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .services(Set.of(testService1, testService2))
                                .build()
                ))
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .services(Set.of(testService2, testService1))
                .build();

        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 25, 9, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 25, 18, 0);

        when(serviceRepository.getMinServiceDuration()).thenReturn(15);
        when(scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);

        assertThatNoException().isThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.of(testService2, testService2), testSchedule));
        verify(scheduleRepository).scheduleExistsInIntervalForDoctorExcludingId(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId(), testSchedule.getId());
        verify(absenceRepository).absenceExistsInIntervalForDoctor(newStartedAt, newFinishedAt,
                testSchedule.getDoctor().getId());
    }

    @Test
    void checkIfScheduleHasAppointmentsShouldThrowApiExceptionWhenAppointmentExistWithinSchedule() {
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .build();

        when(appointmentRepository.existsBySchedule(anyLong(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> scheduleValidator.checkIfScheduleHasAppointments(testSchedule))
                .isExactlyInstanceOf(ApiException.class);
        verify(appointmentRepository).existsBySchedule(testSchedule.getDoctor().getId(),
                testSchedule.getStartedAt(), testSchedule.getFinishedAt());
    }

    @Test
    void checkIfScheduleHasAppointmentsShouldNotThrowApiExceptionWhenNotExistWithinSchedule() {
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .build();

        when(appointmentRepository.existsBySchedule(anyLong(), any(), any())).thenReturn(false);

        assertThatNoException().isThrownBy(() ->
                scheduleValidator.checkIfScheduleHasAppointments(testSchedule));
        verify(appointmentRepository).existsBySchedule(testSchedule.getDoctor().getId(),
                testSchedule.getStartedAt(), testSchedule.getFinishedAt());
    }

    @Test
    void checkIfScheduleHasAppointmentsInOldTimeSlotShouldThrowApiExceptionWhenAppointmentExistsWithin() {
        Set<Long> appointmentIds = Set.of(1L, 2L);
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .build();
        LocalDateTime newStartedAt = testSchedule.getStartedAt().plusHours(3);
        LocalDateTime newFinishedAt = testSchedule.getFinishedAt().minusHours(1);

        when(appointmentRepository.getAppointmentIdsInPreviousScheduleTimeslot(anyLong(), any(), any(), any(), any()))
                .thenReturn(appointmentIds);

        assertThatThrownBy(() -> scheduleValidator
                .checkIfScheduleHasAppointmentsInOldTimeslot(testSchedule, newStartedAt, newFinishedAt))
                .isExactlyInstanceOf(ApiException.class)
                .message().contains(appointmentIds.toString());
        verify(appointmentRepository).getAppointmentIdsInPreviousScheduleTimeslot(testSchedule.getDoctor().getId(),
                testSchedule.getStartedAt(), testSchedule.getFinishedAt(), newStartedAt, newFinishedAt);
    }

    @Test
    void checkIfScheduleHasAppointmentsInOldTimeSlotShouldNotThrowApiExceptionWhenNoAppointmentExistsWithin() {
        Set<Long> appointmentIds = new HashSet<>();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .build();
        LocalDateTime newStartedAt = testSchedule.getStartedAt().plusHours(3);
        LocalDateTime newFinishedAt = testSchedule.getFinishedAt().plusHours(3);

        when(appointmentRepository.getAppointmentIdsInPreviousScheduleTimeslot(anyLong(), any(), any(), any(), any()))
                .thenReturn(appointmentIds);

        assertThatNoException().isThrownBy(() -> scheduleValidator
                .checkIfScheduleHasAppointmentsInOldTimeslot(testSchedule, newStartedAt, newFinishedAt));
        verify(appointmentRepository).getAppointmentIdsInPreviousScheduleTimeslot(testSchedule.getDoctor().getId(),
                testSchedule.getStartedAt(), testSchedule.getFinishedAt(), newStartedAt, newFinishedAt);
    }

    @Test
    void checkIfScheduleHasAppointmentsInOldTimeSlotShouldNotThrowApiExceptionWhenNewTimeSlotContainsOld() {
        Set<Long> appointmentIds = new HashSet<>();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .build();
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 22, 9, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 22, 18, 0))
                .doctor(testDoctor)
                .build();
        LocalDateTime newStartedAt = testSchedule.getStartedAt();
        LocalDateTime newFinishedAt = testSchedule.getFinishedAt().plusHours(3);

        assertThatNoException().isThrownBy(() -> scheduleValidator
                .checkIfScheduleHasAppointmentsInOldTimeslot(testSchedule, newStartedAt, newFinishedAt));
        verify(appointmentRepository, never()).getAppointmentIdsInPreviousScheduleTimeslot(testSchedule.getDoctor().getId(),
                testSchedule.getStartedAt(), testSchedule.getFinishedAt(), newStartedAt, newFinishedAt);
    }

    @Test
    void validateNewTimeslotAndServicesShouldThrowApiExceptionWhenScheduleIsAtWeekend() {
        Schedule testSchedule = Schedule.builder()
                .startedAt(LocalDateTime.of(2025, 11, 14, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 14, 18, 0))
                .doctor(Doctor.builder()
                        .id(1L)
                        .build())
                .services(Set.of(Service.builder()
                        .id(1L)
                        .build()))
                .build();

        LocalDateTime newStartedAt = LocalDateTime.of(2025, 11, 22, 8, 0);
        LocalDateTime newFinishedAt = LocalDateTime.of(2025, 11, 22, 20, 0);

        assertThatThrownBy(() -> scheduleValidator.validateNewTimeslotAndServices(newStartedAt,
                newFinishedAt, testSchedule.getDoctor(), List.copyOf(testSchedule.getServices()), testSchedule))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("weekend");
    }
}