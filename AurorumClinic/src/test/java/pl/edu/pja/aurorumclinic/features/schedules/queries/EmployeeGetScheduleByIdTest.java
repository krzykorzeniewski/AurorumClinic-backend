package pl.edu.pja.aurorumclinic.features.schedules.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmployeeGetScheduleById.class})
@ActiveProfiles("test")
public class EmployeeGetScheduleByIdTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetScheduleById employeeGetScheduleById;

    @Test
    void empGetScheduleByIdShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeGetScheduleById.empGetScheduleById(scheduleId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void empGetScheduleByIdShouldReturnScheduleDtoWithScheduleData() {
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

        Long scheduleId = 1L;
        Schedule testSchedule1 = Schedule.builder()
                .id(scheduleId)
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

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule1));
        when(objectStorageService.generateUrl(anyString())).thenReturn(generatedPictureUrl);

        ResponseEntity<ApiResponse<EmployeeGetScheduleResponse>> responseEntity =
                employeeGetScheduleById.empGetScheduleById(scheduleId);
        assertThat(responseEntity.getBody()).isNotNull();

        EmployeeGetScheduleResponse responseDto = responseEntity.getBody().getData();
        assertThat(responseDto).isNotNull();
        assertThat(responseDto).isEqualTo(expectedResponseDto);

        verify(scheduleRepository).findById(scheduleId);
        verify(objectStorageService).generateUrl(testSchedule1.getDoctor().getProfilePicture());
    }

}
