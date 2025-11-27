package pl.edu.pja.aurorumclinic.features.absences.commands;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorUpdateAbsenceTest {

    DoctorRepository doctorRepository;

    AbsenceRepository absenceRepository;

    AbsenceValidator absenceValidator;

    DoctorUpdateAbsence doctorUpdateAbsence;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        absenceRepository = mock(AbsenceRepository.class);
        absenceValidator = mock(AbsenceValidator.class);
        doctorUpdateAbsence = new DoctorUpdateAbsence(doctorRepository, absenceRepository, absenceValidator);
    }

    @Test
    void docUpdateAbsenceShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;
        Long doctorId = 2L;
        DoctorUpdateAbsence.DocUpdateAbsenceRequest request = new DoctorUpdateAbsence.DocUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> doctorUpdateAbsence.docUpdateAbsence(absenceId, request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("absence");
        verify(absenceRepository).findById(absenceId);
    }

    @Test
    void docUpdateAbsenceShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 2L;
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();
        DoctorUpdateAbsence.DocUpdateAbsenceRequest request = new DoctorUpdateAbsence.DocUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> doctorUpdateAbsence.docUpdateAbsence(absenceId, request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("doctor");
        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docUpdateAbsenceShouldThrowApiAuthExceptionWhenRequestDoctorIdIsNotEqualToAbsenceDoctorId() {
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .doctor(Doctor.builder()
                        .id(420000L)
                        .build())
                .build();
        DoctorUpdateAbsence.DocUpdateAbsenceRequest request = new DoctorUpdateAbsence.DocUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        Assertions.assertThatThrownBy(() -> doctorUpdateAbsence.docUpdateAbsence(absenceId, request, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docUpdateAbsenceShouldThrowUpdateAbsenceWithFieldsFromRequest() {
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .doctor(testDoctor)
                .build();
        DoctorUpdateAbsence.DocUpdateAbsenceRequest request = new DoctorUpdateAbsence.DocUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        doctorUpdateAbsence.docUpdateAbsence(absenceId, request, doctorId);

        assertThat(testAbsence.getName()).isEqualTo(request.name());
        assertThat(testAbsence.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(testAbsence.getFinishedAt()).isEqualTo(request.finishedAt());

        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
        verify(absenceValidator).validateNewTimeslot(request.startedAt(), request.finishedAt(), testDoctor, testAbsence);
    }

}
