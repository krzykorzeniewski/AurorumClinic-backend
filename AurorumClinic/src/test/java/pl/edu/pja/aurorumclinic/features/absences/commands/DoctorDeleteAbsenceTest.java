package pl.edu.pja.aurorumclinic.features.absences.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DoctorDeleteAbsenceTest {

    AbsenceRepository absenceRepository;

    DoctorRepository doctorRepository;

    DoctorDeleteAbsence doctorDeleteAbsence;

    @BeforeEach
    void setUp() {
        absenceRepository = mock(AbsenceRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        doctorDeleteAbsence = new DoctorDeleteAbsence(absenceRepository, doctorRepository);
    }

    @Test
    void docDeleteAbsenceShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;
        Long doctorId = 2L;

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDeleteAbsence.docDeleteAbsence(absenceId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("absence");
        verify(absenceRepository).findById(absenceId);
    }

    @Test
    void docDeleteAbsenceShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 2L;
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDeleteAbsence.docDeleteAbsence(absenceId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("doctor");
        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docDeleteAbsenceShouldThrowApiAuthExceptionWhenAbsenceDoctorIdIsNotEqualToRequestDoctorId() {
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .doctor(Doctor.builder()
                        .id(1000L)
                        .build())
                .build();

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        assertThatThrownBy(() -> doctorDeleteAbsence.docDeleteAbsence(absenceId, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docDeleteAbsenceShouldDeleteAbsenceWhenExistsForIdAndDoctorId() {
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .doctor(testDoctor)
                .build();

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        doctorDeleteAbsence.docDeleteAbsence(absenceId, doctorId);

        ArgumentCaptor<Absence> absenceArgumentCaptor = ArgumentCaptor.forClass(Absence.class);
        verify(absenceRepository).delete(absenceArgumentCaptor.capture());

        Absence deletedAbsence = absenceArgumentCaptor.getValue();
        assertThat(deletedAbsence).isEqualTo(testAbsence);

        verify(absenceRepository).findById(absenceId);
        verify(doctorRepository).findById(doctorId);
    }
}
