package pl.edu.pja.aurorumclinic.features.schedules.queries;

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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorGetScheduleByIdTest {

    DoctorRepository doctorRepository;

    ScheduleRepository scheduleRepository;

    DoctorGetScheduleById doctorGetScheduleById;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        doctorGetScheduleById = new DoctorGetScheduleById(doctorRepository, scheduleRepository);
    }

    @Test
    void docGetScheduleByIdShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 1L;
        Long scheduleId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetScheduleById.docGetScheduleById(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docGetScheduleByIdShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetScheduleById.docGetScheduleById(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void docGetScheduleByIdShouldThrowApiAuthExceptionWhenRequestDocIdIsNotEqualToScheduleDocId() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> doctorGetScheduleById.docGetScheduleById(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void docGetScheduleByIdShouldReturnScheduleDtoWithScheduleValues() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(testDoctor)
                .services(Set.of(Service.builder()
                                .id(1L)
                                .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .build()))
                .build();

        DoctorGetScheduleResponse expectedResponse = DoctorGetScheduleResponse.builder()
                .id(testSchedule.getId())
                .startedAt(testSchedule.getStartedAt())
                .finishedAt(testSchedule.getFinishedAt())
                .services(testSchedule.getServices().stream().map(
                        service -> DoctorGetScheduleResponse.ServiceDto.builder()
                                .id(service.getId())
                                .name(service.getName())
                                .build()).toList())
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        ResponseEntity<ApiResponse<DoctorGetScheduleResponse>> responseEntity =
                doctorGetScheduleById.docGetScheduleById(scheduleId, doctorId);

        assertThat(responseEntity.getBody()).isNotNull();

        DoctorGetScheduleResponse response = responseEntity.getBody().getData();
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(expectedResponse);

        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
    }
}
