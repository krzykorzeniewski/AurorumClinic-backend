package pl.edu.pja.aurorumclinic.features.schedules.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.EmployeeGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmployeeGetAllSchedules.class})
@ActiveProfiles("test")
public class EmployeeGetAllSchedulesTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetAllSchedules employeeGetAllSchedules;

    @Test
    void getAllSchedulesShouldReturnPageOfResponseDtosWithScheduleData() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(10);
        LocalDateTime finished = LocalDateTime.now().plusDays(10);
        Pageable unpaged = Pageable.unpaged();
        Doctor testDoctor = Doctor.builder()
                .id(1L)
                .name("Mariusz")
                .surname("Ujazdowski")
                .profilePicture("superpicture.png")
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build(),
                        Specialization.builder()
                                .id(2L)
                                .name("Psychoterapeuta dorosłych")
                                .build()
                ))
                .build();
        Schedule testSchedule1 = Schedule.builder()
                .id(1L)
                .startedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now().plusHours(12))
                .doctor(testDoctor)
                .services(Set.of(
                        Service.builder()
                                .id(1L)
                                .name("Konsultacja psychiatryczna dorosłych (pierwsza wizyta)")
                                .build(),
                        Service.builder()
                                .id(2L)
                                .name("Konsultacja psychiatryczna dorosłych (kolejna wizyta)")
                                .build()
                ))
                .build();
        String generatedPictureUrl = "https://superpicture.png.aws.something.com";

        EmployeeGetScheduleResponse expectedResponseDto = EmployeeGetScheduleResponse.builder()
                .id(testSchedule1.getId())
                .startedAt(testSchedule1.getStartedAt())
                .finishedAt(testSchedule1.getFinishedAt())
                .doctor(EmployeeGetScheduleResponse.DoctorDto.builder()
                        .id(testSchedule1.getDoctor().getId())
                        .name(testSchedule1.getDoctor().getName())
                        .surname(testSchedule1.getDoctor().getSurname())
                        .profilePicture(generatedPictureUrl)
                        .specializations(testSchedule1.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetScheduleResponse.DoctorDto.SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .services(testSchedule1.getServices().stream().map(service -> EmployeeGetScheduleResponse.ServiceDto.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .build()).toList())
                .build();

        PageImpl<Schedule> returnedPage =
                new PageImpl<>(List.of(testSchedule1), unpaged, 1);

        when(scheduleRepository.findAllSchedulesBetweenDates(any(), any(), any())).thenReturn(returnedPage);
        when(objectStorageService.generateUrl(anyString())).thenReturn(generatedPictureUrl);

        ResponseEntity<ApiResponse<Page<EmployeeGetScheduleResponse>>> responseEntity =
                employeeGetAllSchedules.getAllSchedules(unpaged, startedAt, finished);

        assertThat(responseEntity.getBody()).isNotNull();

        Page<EmployeeGetScheduleResponse> responsePage = responseEntity.getBody().getData();
        assertThat(responsePage).hasSize(1);

        EmployeeGetScheduleResponse responseDto = responsePage.getContent().get(0);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto).isEqualTo(expectedResponseDto);

        verify(scheduleRepository).findAllSchedulesBetweenDates(startedAt, finished, unpaged);
        verify(objectStorageService).generateUrl(testSchedule1.getDoctor().getProfilePicture());
    }

}
