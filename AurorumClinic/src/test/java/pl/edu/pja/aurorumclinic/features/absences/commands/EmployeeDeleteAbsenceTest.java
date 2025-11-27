package pl.edu.pja.aurorumclinic.features.absences.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class EmployeeDeleteAbsenceTest {

    AbsenceRepository absenceRepository;

    EmployeeDeleteAbsence employeeDeleteAbsence;

    @BeforeEach
    void setUp() {
        absenceRepository = mock(AbsenceRepository.class);
        employeeDeleteAbsence = new EmployeeDeleteAbsence(absenceRepository);
    }

    @Test
    void empDeleteAbsenceShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeDeleteAbsence.empDeleteAbsence(absenceId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(absenceRepository).findById(absenceId);
    }

    @Test
    void empDeleteAbsenceShouldDeleteAbsenceWhenExists() {
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .build();

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));

        employeeDeleteAbsence.empDeleteAbsence(absenceId);

        ArgumentCaptor<Absence> absenceArgumentCaptor = ArgumentCaptor.forClass(Absence.class);
        verify(absenceRepository).delete(absenceArgumentCaptor.capture());

        Absence deletedAbsence = absenceArgumentCaptor.getValue();
        assertThat(deletedAbsence).isEqualTo(testAbsence);

        verify(absenceRepository).findById(absenceId);
    }
}