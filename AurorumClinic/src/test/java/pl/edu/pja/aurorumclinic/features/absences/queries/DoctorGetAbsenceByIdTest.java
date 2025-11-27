package pl.edu.pja.aurorumclinic.features.absences.queries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorGetAbsenceByIdTest {

    DoctorRepository doctorRepository;

    AbsenceRepository absenceRepository;

    DoctorGetAbsenceById doctorGetAbsenceById;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        absenceRepository = mock(AbsenceRepository.class);
        doctorGetAbsenceById = new DoctorGetAbsenceById(doctorRepository, absenceRepository);
    }

    @Test
    void docGetAbsenceByIdShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long absenceId = 1L;
        Long doctorId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetAbsenceById.docGetAbsenceById(absenceId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("doctor");
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docGetAbsenceByIdShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(absenceRepository.findDoctorAbsenceDtoById(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetAbsenceById.docGetAbsenceById(absenceId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .message().containsIgnoringCase("absence");
        verify(doctorRepository).findById(doctorId);
        verify(absenceRepository).findDoctorAbsenceDtoById(absenceId, doctorId);
    }

    @Test
    void docGetAbsenceByIdShouldReturnDtoWhenExistsForDoctorIdAndAbsenceId() {
        Long absenceId = 1L;
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        DoctorGetAbsenceResponse expectedResponse = DoctorGetAbsenceResponse.builder()
                .id(absenceId)
                .name("Mariusz L4")
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusDays(7))
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(absenceRepository.findDoctorAbsenceDtoById(anyLong(), anyLong())).thenReturn(Optional.of(expectedResponse));

        ResponseEntity<ApiResponse<DoctorGetAbsenceResponse>> responseEntity =
                doctorGetAbsenceById.docGetAbsenceById(absenceId, doctorId);
        assertThat(responseEntity.getBody()).isNotNull();

        DoctorGetAbsenceResponse response = responseEntity.getBody().getData();

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(expectedResponse);
        verify(doctorRepository).findById(doctorId);
        verify(absenceRepository).findDoctorAbsenceDtoById(absenceId, doctorId);
    }
}
