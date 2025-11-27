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

public class EmployeeCreateAbsenceTest {

    AbsenceRepository absenceRepository;

    AbsenceValidator absenceValidator;

    DoctorRepository doctorRepository;

    EmployeeCreateAbsence employeeCreateAbsence;

    @BeforeEach
    void setUp() {
        absenceRepository = mock(AbsenceRepository.class);
        absenceValidator = mock(AbsenceValidator.class);
        doctorRepository = mock(DoctorRepository.class);
        employeeCreateAbsence = new EmployeeCreateAbsence(absenceRepository, absenceValidator, doctorRepository);
    }

    @Test
    void empCreateAbsenceShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(14);
        Long doctorId = 1L;
        EmployeeCreateAbsence.EmpCreateAbsenceRequest request = new EmployeeCreateAbsence.EmpCreateAbsenceRequest(
                startedAt,
                finishedAt,
                "Mariusz L4",
                doctorId
        );

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeCreateAbsence.empCreateAbsence(request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void empCreateAbsenceShouldSaveAbsenceWithDataFromRequest() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(14);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        EmployeeCreateAbsence.EmpCreateAbsenceRequest request = new EmployeeCreateAbsence.EmpCreateAbsenceRequest(
                startedAt,
                finishedAt,
                "Mariusz L4",
                doctorId
        );

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));

        employeeCreateAbsence.empCreateAbsence(request);

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