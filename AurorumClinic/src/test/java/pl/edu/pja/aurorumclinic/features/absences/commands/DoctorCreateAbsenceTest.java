package pl.edu.pja.aurorumclinic.features.absences.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorCreateAbsenceTest {

    DoctorRepository doctorRepository;

    AbsenceRepository absenceRepository;

    AbsenceValidator absenceValidator;

    DoctorCreateAbsence doctorCreateAbsence;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        absenceRepository = mock(AbsenceRepository.class);
        absenceValidator = mock(AbsenceValidator.class);
        doctorCreateAbsence = new DoctorCreateAbsence(doctorRepository, absenceRepository, absenceValidator);
    }

    @Test
    void docCreateAbsenceShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(14);
        Long doctorId = 1L;
        DoctorCreateAbsence.DocCreateAbsenceRequest request = new DoctorCreateAbsence.DocCreateAbsenceRequest(
                startedAt,
                finishedAt,
                "Święta Bożego Narodzenia"
        );

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorCreateAbsence.docCreateAbsence(request, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docCreateAbsenceShouldSaveAbsenceWithDataFromRequest() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(14);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        DoctorCreateAbsence.DocCreateAbsenceRequest request = new DoctorCreateAbsence.DocCreateAbsenceRequest(
                startedAt,
                finishedAt,
                "Święta Bożego Narodzenia"
        );

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        doctorCreateAbsence.docCreateAbsence(request, doctorId);

        ArgumentCaptor<Absence> absenceArgumentCaptor = ArgumentCaptor.forClass(Absence.class);
        verify(absenceRepository).save(absenceArgumentCaptor.capture());

        Absence savedAbsence = absenceArgumentCaptor.getValue();
        assertThat(savedAbsence.getDoctor()).isEqualTo(testDoctor);
        assertThat(savedAbsence.getStartedAt()).isEqualTo(startedAt);
        assertThat(savedAbsence.getFinishedAt()).isEqualTo(finishedAt);
        assertThat(savedAbsence.getName()).isEqualTo(request.name());

        verify(doctorRepository).findById(doctorId);
        verify(absenceValidator).validateTimeslot(startedAt, finishedAt, testDoctor);
    }
}
