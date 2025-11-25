package pl.edu.pja.aurorumclinic.features.schedules.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {EmployeeGetScheduleByIdAppointments.class})
@ActiveProfiles("test")
public class EmployeeGetScheduleByIdAppointmentsTest {

    @MockitoBean
    ScheduleRepository scheduleRepository;

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    EmployeeGetScheduleByIdAppointments employeeGetScheduleByIdAppointments;

    @Test
    void empGetScheduleByIdAppointmentsShouldThrowApiNotFoundExceptionWhenScheduleIdIsNotFound() {
        Long scheduleId = 1L;

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeGetScheduleByIdAppointments
                .empGetScheduleByIdAppointments(scheduleId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    void empGetScheduleByIdAppointmentsShouldReturnDtoWithAppointmentFields() {
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
        String generatedPictureUrl = "https://superpicture.png.aws.something.com";

        EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse expectedDto =
                EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.builder()
                .id(testAppointment.getId())
                .status(testAppointment.getStatus())
                .startedAt(testAppointment.getStartedAt())
                .description(testAppointment.getDescription())
                .doctor(EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment.getDoctor().getId())
                        .name(testAppointment.getDoctor().getName())
                        .surname(testAppointment.getDoctor().getSurname())
                        .profilePicture(generatedPictureUrl)
                        .specializations(testAppointment.getDoctor().getSpecializations()
                                .stream().map(spec -> EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment.getService().getId())
                        .name(testAppointment.getService().getName())
                        .price(testAppointment.getService().getPrice())
                        .build())
                .payment(EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment.getPayment().getId())
                        .amount(testAppointment.getPayment().getAmount())
                        .status(testAppointment.getPayment().getStatus())
                        .build())
                .patient(EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse.PatientDto.builder()
                        .id(testAppointment.getPatient().getId())
                        .name(testAppointment.getPatient().getName())
                        .surname(testAppointment.getPatient().getSurname())
                        .phoneNumber(testAppointment.getPatient().getPhoneNumber())
                        .email(testAppointment.getPatient().getEmail())
                        .build())
                .build();

        when(scheduleRepository.findById(anyLong())).thenReturn(Optional.of(testSchedule));
        when(appointmentRepository.findAllBySchedule(anyLong(), any(), any())).thenReturn(List.of(testAppointment));
        when(objectStorageService.generateUrl(anyString())).thenReturn(generatedPictureUrl);

        ResponseEntity<ApiResponse<List<EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse>>>
                responseEntity = employeeGetScheduleByIdAppointments.empGetScheduleByIdAppointments(scheduleId);
        assertThat(responseEntity.getBody()).isNotNull();

        List<EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse>
                resultList = responseEntity.getBody().getData();
        assertThat(resultList).hasSize(1);

        EmployeeGetScheduleByIdAppointments.EmployeeGetScheduleAppointmentResponse resultDto = resultList.get(0);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto).isEqualTo(expectedDto);

        verify(scheduleRepository).findById(scheduleId);
        verify(appointmentRepository)
                .findAllBySchedule(testSchedule.getDoctor().getId(), testSchedule.getStartedAt(), testSchedule.getFinishedAt());
        verify(objectStorageService).generateUrl(testAppointment.getDoctor().getProfilePicture());
    }

}
