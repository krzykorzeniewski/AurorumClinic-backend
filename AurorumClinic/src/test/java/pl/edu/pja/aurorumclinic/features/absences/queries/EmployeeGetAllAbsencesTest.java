package pl.edu.pja.aurorumclinic.features.absences.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.EmployeeGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EmployeeGetAllAbsences.class})
@ActiveProfiles("test")
public class EmployeeGetAllAbsencesTest {

    @MockitoBean
    AbsenceRepository absenceRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetAllAbsences employeeGetAllAbsences;

    @Test
    void empGetAllAbsencesShouldReturnPageOfDtos() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(30);
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(30);
        Pageable pageable = Pageable.unpaged();
        Absence testAbsence = Absence.builder()
                .id(21000037L)
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
        PageImpl<Absence> returnedPage = new PageImpl<>(List.of(testAbsence), pageable, 1);

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

        when(absenceRepository.findAllBetween(any(), any(), any())).thenReturn(returnedPage);
        when(objectStorageService.generateUrl(anyString())).thenReturn(generatedUrl);

        ResponseEntity<ApiResponse<Page<EmployeeGetAbsenceResponse>>> responseEntity =
                employeeGetAllAbsences.empGetAllAbsences(pageable, startedAt, finishedAt);
        assertThat(responseEntity.getBody()).isNotNull();

        Page<EmployeeGetAbsenceResponse> responsePage = responseEntity.getBody().getData();
        assertThat(responsePage).isNotEmpty();
        assertThat(responsePage).hasSize(1);

        EmployeeGetAbsenceResponse response = responsePage.getContent().get(0);
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(expectedResponse);

        verify(absenceRepository).findAllBetween(startedAt, finishedAt, pageable);
        verify(objectStorageService).generateUrl(testAbsence.getDoctor().getProfilePicture());
    }



}
