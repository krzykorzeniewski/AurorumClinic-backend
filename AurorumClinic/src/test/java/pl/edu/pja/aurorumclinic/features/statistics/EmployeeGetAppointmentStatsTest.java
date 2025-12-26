package pl.edu.pja.aurorumclinic.features.statistics;

import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.statistics.EmployeeGetAppointmentStats.AllAppointmentStatsResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EmployeeGetAppointmentStats.class})
@ActiveProfiles("test")
class EmployeeGetAppointmentStatsTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    DoctorRepository doctorRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetAppointmentStats controller;

    @Test
    void shouldReturnBasicStatsWithoutDoctorsWhenFetchIsNull() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 1, 31, 23, 59);

        Tuple totalTuple = mock(Tuple.class);
        when(totalTuple.get("scheduled")).thenReturn(10L);
        when(totalTuple.get("finished")).thenReturn(8L);
        when(totalTuple.get("avgDuration")).thenReturn(30.5d);
        when(totalTuple.get("avgRating")).thenReturn(4.2d);

        when(appointmentRepository.getAllAppointmentStatsBetween(startedAt, finishedAt))
                .thenReturn(List.of(totalTuple));

        var resp = controller.getAppointmentStatistics(startedAt, finishedAt, null);

        assertThat(resp).isNotNull();
        ApiResponse<AllAppointmentStatsResponse> body = resp.getBody();
        assertThat(body).isNotNull();

        AllAppointmentStatsResponse data = body.getData();
        assertThat(data).isNotNull();
        assertThat(data.totalScheduled()).isEqualTo(10L);
        assertThat(data.totalFinished()).isEqualTo(8L);
        assertThat(data.avgDuration()).isEqualTo(30.5d);
        assertThat(data.avgRating()).isEqualTo(4.2d);

        assertThat(data.doctors()).isNull();
    }

    @Test
    void shouldReturnAllStatsWithDoctorsWhenFetchAll() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 1, 31, 23, 59);

        Tuple totalTuple = mock(Tuple.class);
        when(totalTuple.get("scheduled")).thenReturn(20L);
        when(totalTuple.get("finished")).thenReturn(15L);
        when(totalTuple.get("avgDuration")).thenReturn(40.0d);
        when(totalTuple.get("avgRating")).thenReturn(4.5d);

        when(appointmentRepository.getAllAppointmentStatsBetween(startedAt, finishedAt))
                .thenReturn(List.of(totalTuple));

        Doctor doctor = Doctor.builder()
                .id(1L)
                .name("Jan")
                .surname("Kowalski")
                .profilePicture("jan_kowalski.jpg")
                .specializations(Set.of())
                .build();

        when(doctorRepository.findAll()).thenReturn(List.of(doctor));
        when(objectStorageService.generateUrl("jan_kowalski.jpg"))
                .thenReturn("https://cdn.example/jan_kowalski.jpg");

        Tuple doctorTotalTuple = mock(Tuple.class);
        when(doctorTotalTuple.get("scheduled")).thenReturn(5L);
        when(doctorTotalTuple.get("finished")).thenReturn(4L);
        when(doctorTotalTuple.get("avgDuration")).thenReturn(35.0d);
        when(doctorTotalTuple.get("avgRating")).thenReturn(4.8d);

        when(appointmentRepository.getDoctorAppointmentStatsBetween(eq(1L), eq(startedAt), eq(finishedAt)))
                .thenReturn(List.of(doctorTotalTuple));

        Tuple serviceTuple = mock(Tuple.class);
        when(serviceTuple.get("scheduled")).thenReturn(3L);
        when(serviceTuple.get("finished")).thenReturn(3L);
        when(serviceTuple.get("avgDuration")).thenReturn(30.0d);
        when(serviceTuple.get("avgRating")).thenReturn(5.0d);
        when(serviceTuple.get("servId")).thenReturn(100L);
        when(serviceTuple.get("servName")).thenReturn("Konsultacja psychiatryczna");

        when(appointmentRepository.getDoctorAppointmentStatsPerServiceBetween(eq(1L), eq(startedAt), eq(finishedAt)))
                .thenReturn(List.of(serviceTuple));

        var resp = controller.getAppointmentStatistics(startedAt, finishedAt, "all");

        assertThat(resp).isNotNull();
        ApiResponse<AllAppointmentStatsResponse> body = resp.getBody();
        assertThat(body).isNotNull();

        AllAppointmentStatsResponse data = body.getData();
        assertThat(data).isNotNull();

        assertThat(data.totalScheduled()).isEqualTo(20L);
        assertThat(data.totalFinished()).isEqualTo(15L);
        assertThat(data.avgDuration()).isEqualTo(40.0d);
        assertThat(data.avgRating()).isEqualTo(4.5d);

        assertThat(data.doctors()).isNotNull();
        assertThat(data.doctors()).hasSize(1);

        var docStats = data.doctors().get(0);
        assertThat(docStats.doctorId()).isEqualTo(1L);
        assertThat(docStats.name()).isEqualTo("Jan");
        assertThat(docStats.surname()).isEqualTo("Kowalski");
        assertThat(docStats.profilePicture()).isEqualTo("https://cdn.example/jan_kowalski.jpg");

        assertThat(docStats.total()).isNotNull();
        assertThat(docStats.total()).hasSize(1);

        var docTotal = docStats.total().get(0);
        assertThat(docTotal.totalScheduled()).isEqualTo(5L);
        assertThat(docTotal.totalFinished()).isEqualTo(4L);
        assertThat(docTotal.avgDuration()).isEqualTo(35.0d);
        assertThat(docTotal.avgRating()).isEqualTo(4.8d);

        assertThat(docTotal.services()).hasSize(1);
        var servStats = docTotal.services().get(0);
        assertThat(servStats.scheduled()).isEqualTo(3L);
        assertThat(servStats.finished()).isEqualTo(3L);
        assertThat(servStats.avgDuration()).isEqualTo(30.0d);
        assertThat(servStats.avgRating()).isEqualTo(5.0d);
        assertThat(servStats.service().id()).isEqualTo(100L);
        assertThat(servStats.service().name()).isEqualTo("Konsultacja psychiatryczna");
    }
}
