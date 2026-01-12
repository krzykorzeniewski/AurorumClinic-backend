package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetDoctorByIdSchedules.class})
@ActiveProfiles("test")
class GetDoctorByIdSchedulesTest {

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @Autowired
    GetDoctorByIdSchedules controller;

    @Test
    void getDoctorSchedulesShouldThrowApiNotFoundExceptionWhenDoctorIdNotFound() {
        Long doctorId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDoctorSchedules(doctorId, startedAt, finishedAt))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(scheduleRepository);
    }

    @Test
    void getDoctorSchedulesShouldReturnMappedSchedulesWithServices() {
        Long doctorId = 1L;
        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        Doctor doctor = Doctor.builder().id(doctorId).build();
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));

        Service consult = Service.builder().id(101L).name("Konsultacja psychiatryczna").build();
        Service therapy = Service.builder().id(102L).name("Psychoterapia").build();

        Schedule s1 = Schedule.builder()
                .id(1001L)
                .startedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .finishedAt(LocalDateTime.of(2026, 1, 1, 11, 0))
                .services(Set.of(consult, therapy))
                .build();

        Schedule s2 = Schedule.builder()
                .id(1002L)
                .startedAt(LocalDateTime.of(2026, 1, 1, 11, 0))
                .finishedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .services(Set.of(consult))
                .build();

        when(scheduleRepository.findAllByDoctorIdAndBetween(doctorId, startedAt, finishedAt))
                .thenReturn(List.of(s1, s2));

        var responseEntity = controller.getDoctorSchedules(doctorId, startedAt, finishedAt);
        Object apiResponse = responseEntity.getBody();
        @SuppressWarnings("unchecked")
        List<GetDoctorByIdSchedules.GetDoctorSchedulesResponse> result =
                (List<GetDoctorByIdSchedules.GetDoctorSchedulesResponse>) extractApiResponseData(apiResponse);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).id()).isEqualTo(1001L);
        assertThat(result.get(0).services())
                .extracting(GetDoctorByIdSchedules.GetDoctorSchedulesResponse.ServiceDto::name)
                .containsExactlyInAnyOrder("Konsultacja psychiatryczna", "Psychoterapia");

        assertThat(result.get(1).id()).isEqualTo(1002L);
        assertThat(result.get(1).services())
                .extracting(GetDoctorByIdSchedules.GetDoctorSchedulesResponse.ServiceDto::name)
                .containsExactly("Konsultacja psychiatryczna");

        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findAllByDoctorIdAndBetween(doctorId, startedAt, finishedAt);
        verifyNoMoreInteractions(doctorRepository, scheduleRepository);
    }

    private static Object extractApiResponseData(Object apiResponse) {
        if (apiResponse == null) return null;
        for (String methodName : List.of("data", "getData", "result", "getResult", "payload", "getPayload")) {
            try {
                Method m = apiResponse.getClass().getMethod(methodName);
                return m.invoke(apiResponse);
            } catch (Exception ignored) { }
        }
        fail("Nie udało się wyciągnąć data/result z ApiResponse. Podeślij klasę ApiResponse, dopasuję asercje 1:1.");
        return null;
    }
}
