package pl.edu.pja.aurorumclinic.features.absences;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class AbsenceValidatorTest {

    ScheduleRepository scheduleRepository;

    AbsenceRepository absenceRepository;

    AbsenceValidator absenceValidator;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(ScheduleRepository.class);
        absenceRepository = mock(AbsenceRepository.class);
        absenceValidator = new AbsenceValidator(scheduleRepository, absenceRepository);
    }

    @Test
    void validateTimeslotShouldThrowApiExceptionWhenStartDateIsAfterFinishDate() {
        LocalDateTime startedAt = LocalDateTime.now().plusHours(1);
        LocalDateTime finishedAt = LocalDateTime.now();
        Doctor testDoctor = Doctor.builder().build();

        assertThatThrownBy(() -> absenceValidator.validateTimeslot(startedAt, finishedAt, testDoctor))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("date");
    }

    @Test
    void validateTimeslotShouldThrowApiExceptionWhenScheduleExistsBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> absenceValidator.validateTimeslot(startedAt, finishedAt, testDoctor))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing schedule");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
    }

    @Test
    void validateTimeslotShouldThrowApiExceptionWhenAbsenceExistsBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> absenceValidator.validateTimeslot(startedAt, finishedAt, testDoctor))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing absence");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
        verify(absenceRepository).absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
    }

    @Test
    void validateTimeslotShouldNotThrowWhenScheduleOrAbsenceDoNotExistsBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);

        assertThatNoException().isThrownBy(() -> absenceValidator.validateTimeslot(startedAt, finishedAt, testDoctor));
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
        verify(absenceRepository).absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
    }

    @Test
    void validateNewTimeslotShouldThrowApiExceptionWhenStartDateIsAfterFinishDate() {
        LocalDateTime startedAt = LocalDateTime.now().plusHours(1);
        LocalDateTime finishedAt = LocalDateTime.now();
        Doctor testDoctor = Doctor.builder().build();
        Absence testAbsence = Absence.builder().build();

        assertThatThrownBy(() -> absenceValidator.validateNewTimeslot(startedAt, finishedAt, testDoctor, testAbsence))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("date");
    }

    @Test
    void validateNewTimeslotShouldThrowApiExceptionWhenScheduleExistsBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 2L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> absenceValidator.validateNewTimeslot(startedAt, finishedAt, testDoctor, testAbsence))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing schedule");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
    }

    @Test
    void validateNewTimeslotShouldThrowApiExceptionWhenOtherAbsenceExistsBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 2L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong()))
                .thenReturn(true);

        assertThatThrownBy(() -> absenceValidator.validateNewTimeslot(startedAt, finishedAt, testDoctor, testAbsence))
                .isExactlyInstanceOf(ApiException.class)
                .message().containsIgnoringCase("existing absence");
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
        verify(absenceRepository).absenceExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, absenceId);
    }

    @Test
    void validateNewTimeslotShouldNotThrowApiExceptionWhenOtherAbsenceOrScheduleDontExistBetweenStartedAtAndFinishedAt() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 2L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();

        when(scheduleRepository.scheduleExistsInIntervalForDoctor(any(), any(), anyLong())).thenReturn(false);
        when(absenceRepository.absenceExistsInIntervalForDoctorExcludingId(any(), any(), anyLong(), anyLong()))
                .thenReturn(false);

        assertThatNoException().isThrownBy(
                () -> absenceValidator.validateNewTimeslot(startedAt, finishedAt, testDoctor, testAbsence));
        verify(scheduleRepository).scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId);
        verify(absenceRepository).absenceExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, absenceId);
    }

}
