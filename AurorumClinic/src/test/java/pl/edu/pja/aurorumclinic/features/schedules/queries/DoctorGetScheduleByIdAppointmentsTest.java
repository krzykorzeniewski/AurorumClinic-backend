package pl.edu.pja.aurorumclinic.features.schedules.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DoctorGetScheduleByIdAppointmentsTest {

    DoctorRepository doctorRepository;

    ScheduleRepository scheduleRepository;

    AppointmentRepository appointmentRepository;

    DoctorGetScheduleByIdAppointments doctorGetScheduleByIdAppointments;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        appointmentRepository = mock(AppointmentRepository.class);
        doctorGetScheduleByIdAppointments =
                new DoctorGetScheduleByIdAppointments(doctorRepository, scheduleRepository, appointmentRepository);
    }

    @Test
    void docGetScheduleByIdAppointmentsShouldThrowApiNotFoundExceptionWhenDoctorIdIsNotFound() {
        Long doctorId = 1L;
        Long scheduleId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetScheduleByIdAppointments.docGetScheduleByIdAppointments(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
    }

    @Test
    void docGetScheduleByIdAppointmentsShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorGetScheduleByIdAppointments.docGetScheduleByIdAppointments(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void docGetScheduleByIdAppointmentsShouldThrowApiAuthExceptionWhenScheduleDocIdIsNotEqualToRequestDocId() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(Doctor.builder()
                        .id(3L)
                        .build())
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> doctorGetScheduleByIdAppointments.docGetScheduleByIdAppointments(scheduleId, doctorId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void docGetScheduleByIdAppointmentsShouldReturnDtoWithAppointmentFields() {
        Long doctorId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(doctorId)
                .build();
        Long scheduleId = 2L;
        Schedule testSchedule = Schedule.builder()
                .id(scheduleId)
                .doctor(testDoctor)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(420L)
                .status(AppointmentStatus.CREATED)
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().minusMinutes(30))
                .description("opis")
                .doctor(testDoctor)
                .service(Service.builder()
                        .id(42L)
                        .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .price(BigDecimal.valueOf(350))
                        .build())
                .payment(Payment.builder()
                        .id(50L)
                        .amount(BigDecimal.valueOf(350))
                        .status(PaymentStatus.COMPLETED)
                        .build())
                .patient(Patient.builder()
                        .id(402L)
                        .name("Mariusz")
                        .surname("Kowalski")
                        .phoneNumber("123123123")
                        .email("mariusz@example.com")
                        .build())
                .build();

        DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse expectedResponse = DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse.builder()
                .id(testAppointment.getId())
                .status(testAppointment.getStatus())
                .startedAt(testAppointment.getStartedAt())
                .description(testAppointment.getDescription())
                .service(DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment.getService().getId())
                        .name(testAppointment.getService().getName())
                        .price(testAppointment.getService().getPrice())
                        .build())
                .payment(DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment.getPayment().getId())
                        .amount(testAppointment.getPayment().getAmount())
                        .status(testAppointment.getPayment().getStatus())
                        .build())
                .patient(DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse.PatientDto.builder()
                        .id(testAppointment.getPatient().getId())
                        .name(testAppointment.getPatient().getName())
                        .surname(testAppointment.getPatient().getSurname())
                        .phoneNumber(testAppointment.getPatient().getPhoneNumber())
                        .email(testAppointment.getPatient().getEmail())
                        .build())
                .build();

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findAllBySchedule(anyLong(), any(), any())).thenReturn(List.of(testAppointment));

        ResponseEntity<ApiResponse<List<DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse>>> responseEntity =
                doctorGetScheduleByIdAppointments.docGetScheduleByIdAppointments(scheduleId, doctorId);
        assertThat(responseEntity.getBody()).isNotNull();

        List<DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse> dtoList = responseEntity.getBody().getData();
        assertThat(dtoList).hasSize(1);

        DoctorGetScheduleByIdAppointments.DoctorGetScheduleAppointmentResponse resultDto = dtoList.get(0);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto).isEqualTo(expectedResponse);

        verify(doctorRepository).findById(doctorId);
        verify(scheduleRepository).findById(scheduleId);
        verify(appointmentRepository).findAllBySchedule(doctorId, testSchedule.getStartedAt(), testSchedule.getFinishedAt());
    }
}
