package pl.edu.pja.aurorumclinic.features.absences.queries;

import org.assertj.core.api.Assertions;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorGetAllAbsencesTest {

    DoctorRepository doctorRepository;

    AbsenceRepository absenceRepository;

    DoctorGetAllAbsences doctorGetAllAbsences;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        absenceRepository = mock(AbsenceRepository.class);
        doctorGetAllAbsences = new DoctorGetAllAbsences(absenceRepository, doctorRepository);
    }

    @Test
    void docGetAllAbsencesShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(30);
        Long doctorId = 1L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetAllAbsences.docGetAllAbsences(startedAt, finishedAt, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docGetAllAbsencesShouldReturnValidDtosForDoctorId() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(30);
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(30);
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();

        DoctorGetAbsenceResponse response1 = DoctorGetAbsenceResponse.builder()
                .id(1L)
                .name("Mariusz L4")
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusDays(7))
                .build();

        DoctorGetAbsenceResponse response2 = DoctorGetAbsenceResponse.builder()
                .id(2L)
                .name("Mariusz znowu poszedł na L4 ...")
                .startedAt(LocalDateTime.now().plusDays(15))
                .finishedAt(LocalDateTime.now().plusDays(22))
                .build();
        List<DoctorGetAbsenceResponse> expectedResponse = List.of(response1, response2);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(absenceRepository.findAllDoctorAbsenceDtosBetween(any(), any(), anyLong())).thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<List<DoctorGetAbsenceResponse>>> responseEntity =
                doctorGetAllAbsences.docGetAllAbsences(startedAt, finishedAt, doctorId);
        assertThat(responseEntity.getBody()).isNotNull();

        List<DoctorGetAbsenceResponse> responseList = responseEntity.getBody().getData();
        assertThat(responseList).isNotEmpty();
        assertThat(responseList).hasSize(2);
        assertThat(responseList).containsExactlyInAnyOrderElementsOf(expectedResponse);

        verify(doctorRepository).findById(doctorId);
        verify(absenceRepository).findAllDoctorAbsenceDtosBetween(startedAt, finishedAt, doctorId);
    }

}
