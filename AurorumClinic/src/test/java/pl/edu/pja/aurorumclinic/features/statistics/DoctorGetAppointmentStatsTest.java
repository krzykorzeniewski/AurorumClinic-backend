package pl.edu.pja.aurorumclinic.features.statistics;

import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {DoctorGetAppointmentStats.class})
@ActiveProfiles("test")
class DoctorGetAppointmentStatsTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    DoctorRepository doctorRepository;

    @Autowired
    DoctorGetAppointmentStats controller;

    @Test
    void shouldThrowNotFoundWhenDoctorDoesNotExist() {
        Long doctorId = 10L;
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                controller.getDoctorAppointmentStats(doctorId, from, to, null)
        ).isExactlyInstanceOf(ApiNotFoundException.class);

        verify(doctorRepository).findById(doctorId);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void shouldReturnTotalsOnlyWhenFetchIsNotAll() {
        Long doctorId = 5L;
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        Doctor doc = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doc));

        Tuple totals = mock(Tuple.class);
        when(totals.get("scheduled")).thenReturn(20L);
        when(totals.get("finished")).thenReturn(15L);
        when(totals.get("avgDuration")).thenReturn(45.0);
        when(totals.get("avgRating")).thenReturn(4.5);

        when(appointmentRepository.getDoctorAppointmentStatsBetween(eq(doctorId), any(), any()))
                .thenReturn(List.of(totals));

        var resp = controller.getDoctorAppointmentStats(doctorId, from, to, null);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<DoctorGetAppointmentStats.DoctorAppointmentStatsResponse> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNotNull();

        var data = body.getData();
        assertThat(data.totalScheduled()).isEqualTo(20L);
        assertThat(data.totalFinished()).isEqualTo(15L);
        assertThat(data.avgDuration()).isEqualTo(45.0);
        assertThat(data.avgRating()).isEqualTo(4.5);
        assertThat(data.services()).isNull();

        verify(doctorRepository).findById(doctorId);
        verify(appointmentRepository).getDoctorAppointmentStatsBetween(eq(doctorId), any(), any());
        verifyNoMoreInteractions(appointmentRepository);
    }

    @Test
    void shouldReturnTotalsAndServicesWhenFetchAll() {
        Long doctorId = 7L;
        LocalDateTime from = LocalDateTime.now().minusDays(90);
        LocalDateTime to = LocalDateTime.now();

        Doctor doc = Doctor.builder()
                .id(doctorId)
                .build();

        when(doctorRepository.findById(doctorId))
                .thenReturn(Optional.of(doc));

        Tuple totals = mock(Tuple.class);
        when(totals.get("scheduled")).thenReturn(40L);
        when(totals.get("finished")).thenReturn(30L);
        when(totals.get("avgDuration")).thenReturn(50.0);
        when(totals.get("avgRating")).thenReturn(4.2);

        when(appointmentRepository.getDoctorAppointmentStatsBetween(eq(doctorId), any(), any()))
                .thenReturn(List.of(totals));

        Tuple s1 = mock(Tuple.class);
        when(s1.get("scheduled")).thenReturn(25L);
        when(s1.get("finished")).thenReturn(18L);
        when(s1.get("avgDuration")).thenReturn(55.0);
        when(s1.get("avgRating")).thenReturn(4.6);
        when(s1.get("servId")).thenReturn(100L);
        when(s1.get("servName")).thenReturn("Konsultacja psychiatryczna");

        Tuple s2 = mock(Tuple.class);
        when(s2.get("scheduled")).thenReturn(15L);
        when(s2.get("finished")).thenReturn(12L);
        when(s2.get("avgDuration")).thenReturn(40.0);
        when(s2.get("avgRating")).thenReturn(3.9);
        when(s2.get("servId")).thenReturn(101L);
        when(s2.get("servName")).thenReturn("Kontrola psychiatryczna");

        when(appointmentRepository.getDoctorAppointmentStatsPerServiceBetween(eq(doctorId), any(), any()))
                .thenReturn(List.of(s1, s2));

        var resp = controller.getDoctorAppointmentStats(doctorId, from, to, "all");

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<DoctorGetAppointmentStats.DoctorAppointmentStatsResponse> body = resp.getBody();
        var data = body.getData();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(data).isNotNull();

        assertThat(data.totalScheduled()).isEqualTo(40L);
        assertThat(data.totalFinished()).isEqualTo(30L);
        assertThat(data.avgDuration()).isEqualTo(50.0);
        assertThat(data.avgRating()).isEqualTo(4.2);

        assertThat(data.services()).isNotNull();
        assertThat(data.services()).hasSize(2);

        var srv1 = data.services().get(0);
        assertThat(srv1.scheduled()).isEqualTo(25L);
        assertThat(srv1.finished()).isEqualTo(18L);
        assertThat(srv1.avgDuration()).isEqualTo(55.0);
        assertThat(srv1.avgRating()).isEqualTo(4.6);
        assertThat(srv1.service().id()).isEqualTo(100L);
        assertThat(srv1.service().name()).isEqualTo("Konsultacja psychiatryczna");

        var srv2 = data.services().get(1);
        assertThat(srv2.scheduled()).isEqualTo(15L);
        assertThat(srv2.finished()).isEqualTo(12L);
        assertThat(srv2.avgDuration()).isEqualTo(40.0);
        assertThat(srv2.avgRating()).isEqualTo(3.9);
        assertThat(srv2.service().id()).isEqualTo(101L);
        assertThat(srv2.service().name()).isEqualTo("Kontrola psychiatryczna");

        verify(doctorRepository).findById(doctorId);
        verify(appointmentRepository).getDoctorAppointmentStatsBetween(eq(doctorId), any(), any());
        verify(appointmentRepository).getDoctorAppointmentStatsPerServiceBetween(eq(doctorId), any(), any());
        verifyNoMoreInteractions(appointmentRepository);
    }
}
