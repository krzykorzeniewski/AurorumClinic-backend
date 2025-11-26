package pl.edu.pja.aurorumclinic.features.absences.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.pja.aurorumclinic.features.absences.AbsenceValidator;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class EmployeeUpdateAbsenceTest {

    AbsenceRepository absenceRepository;

    AbsenceValidator absenceValidator;

    EmployeeUpdateAbsence employeeUpdateAbsence;

    @BeforeEach
    void setUp() {
        absenceRepository = mock(AbsenceRepository.class);
        absenceValidator = mock(AbsenceValidator.class);
        employeeUpdateAbsence = new EmployeeUpdateAbsence(absenceRepository, absenceValidator);
    }

    @Test
    void empUpdateAbsenceShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;
        EmployeeUpdateAbsence.EmpUpdateAbsenceRequest request = new EmployeeUpdateAbsence.EmpUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz 425672 L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeUpdateAbsence.empUpdateAbsence(absenceId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(absenceRepository).findById(absenceId);
    }

    @Test
    void empUpdateAbsenceShouldUpdateAbsenceWithFieldsFromDtoWhenAbsenceIdIsFound() {
        Long absenceId = 1L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();
        EmployeeUpdateAbsence.EmpUpdateAbsenceRequest request = new EmployeeUpdateAbsence.EmpUpdateAbsenceRequest(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "Mariusz 425672 L4"
        );

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));

        employeeUpdateAbsence.empUpdateAbsence(absenceId, request);

        assertThat(testAbsence.getName()).isEqualTo(request.name());
        assertThat(testAbsence.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(testAbsence.getFinishedAt()).isEqualTo(request.finishedAt());
        verify(absenceRepository).findById(absenceId);
        verify(absenceValidator).validateNewTimeslot(request.startedAt(), request.finishedAt(),
                testAbsence.getDoctor(), testAbsence);
    }

}
