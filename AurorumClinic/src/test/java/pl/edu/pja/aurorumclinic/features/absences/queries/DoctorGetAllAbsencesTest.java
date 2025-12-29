package pl.edu.pja.aurorumclinic.features.absences.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        Long doctorId = 1L;
        Pageable pageable = Pageable.unpaged();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetAllAbsences.docGetAllAbsences(doctorId, pageable))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docGetAllAbsencesShouldReturnValidDtosForDoctorId() {
        Long doctorId = 2L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Pageable pageable = Pageable.unpaged();

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
        Page<DoctorGetAbsenceResponse> expectedResponse = new PageImpl<>(List.of(response1, response2), pageable, 2);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(absenceRepository.findAllDoctorAbsenceDtos(anyLong(), any())).thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<Page<DoctorGetAbsenceResponse>>> responseEntity =
                doctorGetAllAbsences.docGetAllAbsences(doctorId, pageable);
        assertThat(responseEntity.getBody()).isNotNull();

        Page<DoctorGetAbsenceResponse> responsePage = responseEntity.getBody().getData();
        assertThat(responsePage).isNotEmpty();
        assertThat(responsePage).hasSize(2);
        assertThat(responsePage).containsExactlyInAnyOrderElementsOf(expectedResponse);

        verify(doctorRepository).findById(doctorId);
        verify(absenceRepository).findAllDoctorAbsenceDtos(doctorId, pageable);
    }

}
