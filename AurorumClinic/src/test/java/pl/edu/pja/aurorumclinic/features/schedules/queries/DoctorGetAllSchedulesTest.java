package pl.edu.pja.aurorumclinic.features.schedules.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.pja.aurorumclinic.features.schedules.queries.shared.DoctorGetScheduleResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorGetAllSchedulesTest {

    DoctorRepository doctorRepository;

    ScheduleRepository scheduleRepository;

    DoctorGetAllSchedules doctorGetAllSchedules;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        doctorGetAllSchedules = new DoctorGetAllSchedules(doctorRepository, scheduleRepository);
    }

    @Test
    void getDoctorSchedulesShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 1L;
        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime finishedAt = startedAt.plusDays(30);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetAllSchedules.getDoctorSchedules(startedAt, finishedAt, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void getDoctorSchedulesShouldReturnCorrectSchedulesDto() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(15);
        LocalDateTime finishedAt = startedAt.plusDays(100);
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Schedule testSchedule = Schedule.builder()
                .id(1L)
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(10))
                .doctor(testDoctor)
                .services(Set.of(Service.builder()
                                .id(1L)
                                .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .build()))
                .build();
        DoctorGetScheduleResponse expectedResponseDto = DoctorGetScheduleResponse.builder()
                .id(testSchedule.getId())
                .startedAt(testSchedule.getStartedAt())
                .finishedAt(testSchedule.getFinishedAt())
                .services(testSchedule.getServices().stream().map(
                        service -> DoctorGetScheduleResponse.ServiceDto.builder()
                                .id(service.getId())
                                .name(service.getName())
                                .build()
                ).toList())
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findAllByDoctorIdAndBetween(anyLong(), any(), any())).thenReturn(List.of(testSchedule));

        ResponseEntity<ApiResponse<List<DoctorGetScheduleResponse>>> resultResponse =
                doctorGetAllSchedules.getDoctorSchedules(startedAt, finishedAt, doctorId);
        assertThat(resultResponse.getBody()).isNotNull();

        List<DoctorGetScheduleResponse> resultList = resultResponse.getBody().getData();
        assertThat(resultList).hasSize(1);

        DoctorGetScheduleResponse resultDto = resultList.get(0);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto)
                .isEqualTo(expectedResponseDto);

        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findAllByDoctorIdAndBetween(doctorId, startedAt, finishedAt);
    }
}
