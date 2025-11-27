package pl.edu.pja.aurorumclinic.features.absences.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.EmployeeGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EmployeeGetAbsenceById.class})
@ActiveProfiles("test")
public class EmployeeGetAbsenceByIdTest {

    @MockitoBean
    AbsenceRepository absenceRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetAbsenceById employeeGetAbsenceById;

    @Test
    void empGetAbsenceByIdShouldThrowApiNotFoundExceptionWhenAbsenceIdIsNotFound() {
        Long absenceId = 1L;

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeGetAbsenceById.empGetAbsenceById(absenceId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(absenceRepository).findById(absenceId);
    }

    @Test
    void empGetAbsenceByIdShouldReturnDtoForAbsenceId() {
        Long absenceId = 210037L;
        Absence testAbsence = Absence.builder()
                .id(absenceId)
                .name("Mariusz znowu na L4")
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusDays(7))
                .doctor(Doctor.builder()
                        .id(2L)
                        .name("Mariusz")
                        .surname("Kowalski")
                        .profilePicture("example.pmg")
                        .specializations(Set.of(
                                Specialization.builder()
                                        .id(1L)
                                        .name("Psychiatra dorosłych")
                                        .build(),
                                Specialization.builder()
                                        .id(2L)
                                        .name("Psychoterapeuta")
                                        .build()
                        ))
                        .build())
                .build();
        String generatedUrl = "http://example.png.sigma.co";

        when(absenceRepository.findById(anyLong())).thenReturn(Optional.of(testAbsence));
        when(objectStorageService.generateUrl(anyString())).thenReturn(generatedUrl);

        EmployeeGetAbsenceResponse expectedResponse = EmployeeGetAbsenceResponse.builder()
                .name(testAbsence.getName())
                .id(testAbsence.getId())
                .startedAt(testAbsence.getStartedAt())
                .finishedAt(testAbsence.getFinishedAt())
                .doctor(EmployeeGetAbsenceResponse.DoctorDto.builder()
                        .id(testAbsence.getDoctor().getId())
                        .name(testAbsence.getDoctor().getName())
                        .surname(testAbsence.getDoctor().getSurname())
                        .profilePicture(generatedUrl)
                        .specializations(testAbsence.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetAbsenceResponse.DoctorDto
                                        .SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .build();

        ResponseEntity<ApiResponse<EmployeeGetAbsenceResponse>> responseEntity =
                employeeGetAbsenceById.empGetAbsenceById(absenceId);
        assertThat(responseEntity.getBody()).isNotNull();

        EmployeeGetAbsenceResponse response = responseEntity.getBody().getData();
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(expectedResponse);

        verify(absenceRepository).findById(absenceId);
        verify(objectStorageService).generateUrl(testAbsence.getDoctor().getProfilePicture());
    }
}
