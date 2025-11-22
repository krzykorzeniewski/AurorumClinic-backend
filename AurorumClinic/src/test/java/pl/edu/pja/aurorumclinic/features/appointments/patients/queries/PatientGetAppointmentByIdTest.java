package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PatientGetAppointmentById.class})
@ActiveProfiles("test")
public class PatientGetAppointmentByIdTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    PatientGetAppointmentById patientGetAppointmentById;

    @Test
    void shouldThrowApiNotFoundWhenAppointmentIdIsNotFound() {
        Patient testPatient = Patient.builder()
                .id(1L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.FINISHED)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .description("Lęki")
                .doctor(Doctor.builder()
                        .id(1L)
                        .name("Mariusz")
                        .surname("Mariuszowski")
                        .profilePicture("example.pmg")
                        .specializations(Set.of(Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build()))
                        .build())
                .service(Service.builder()
                        .id(1L)
                        .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .price(BigDecimal.valueOf(10000))
                        .build())
                .payment(Payment.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(10000))
                        .status(PaymentStatus.CREATED)
                        .build())
                .patient(testPatient)
                .build();

        when(appointmentRepository.getAppointmentByIdAndPatientId(anyLong(), anyLong())).thenReturn(null);

        assertThatThrownBy(
                () -> patientGetAppointmentById.getAppointment(testAppointment.getId(), testPatient.getId())
        ).isExactlyInstanceOf(ApiNotFoundException.class);
        verify(appointmentRepository).getAppointmentByIdAndPatientId(testAppointment.getId(), testPatient.getId());
    }

    @Test
    void shouldThrowApiAuthExceptiondWhenAppointmentPatientIdDoesNotMatchRequestId() {
        Patient testPatient = Patient.builder()
                .id(1L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.FINISHED)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .description("Lęki")
                .doctor(Doctor.builder()
                        .id(1L)
                        .name("Mariusz")
                        .surname("Mariuszowski")
                        .profilePicture("example.pmg")
                        .specializations(Set.of(Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build()))
                        .build())
                .service(Service.builder()
                        .id(1L)
                        .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .price(BigDecimal.valueOf(10000))
                        .build())
                .payment(Payment.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(10000))
                        .status(PaymentStatus.CREATED)
                        .build())
                .patient(Patient.builder()
                        .id(2L) //different thant testPatient
                        .build())
                .build();

        when(appointmentRepository.getAppointmentByIdAndPatientId(anyLong(), anyLong())).thenReturn(testAppointment);

        assertThatThrownBy(
                () -> patientGetAppointmentById.getAppointment(testAppointment.getId(), testPatient.getId())
        ).isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(appointmentRepository).getAppointmentByIdAndPatientId(testAppointment.getId(), testPatient.getId());
    }

    @Test
    void shouldReturnDtoWithCorrectAppointmentFieldsForPatientId() {
        Patient testPatient = Patient.builder()
                .id(1L)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(1L)
                .status(AppointmentStatus.FINISHED)
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .description("Lęki")
                .doctor(Doctor.builder()
                        .id(1L)
                        .name("Mariusz")
                        .surname("Mariuszowski")
                        .profilePicture("example.pmg")
                        .specializations(Set.of(Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build()))
                        .build())
                .service(Service.builder()
                        .id(1L)
                        .name("Konsultacja psychiatryczna (kolejna wizyta)")
                        .price(BigDecimal.valueOf(10000))
                        .build())
                .payment(Payment.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(10000))
                        .status(PaymentStatus.CREATED)
                        .build())
                .patient(testPatient)
                .build();
        String imageUrl = "some image url";
        PatientGetAppointmentResponse testResponse = PatientGetAppointmentResponse.builder()
                .id(testAppointment.getId())
                .status(testAppointment.getStatus())
                .startedAt(testAppointment.getStartedAt())
                .description(testAppointment.getDescription())
                .doctor(PatientGetAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment.getDoctor().getId())
                        .name(testAppointment.getDoctor().getName())
                        .surname(testAppointment.getDoctor().getSurname())
                        .profilePicture(imageUrl)
                        .specializations(testAppointment.getDoctor().getSpecializations()
                                .stream().map(spec -> PatientGetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(PatientGetAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment.getService().getId())
                        .name(testAppointment.getService().getName())
                        .price(testAppointment.getService().getPrice())
                        .build())
                .payment(PatientGetAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment.getPayment().getId())
                        .amount(testAppointment.getPayment().getAmount())
                        .status(testAppointment.getPayment().getStatus())
                        .build())
                .build();

        when(appointmentRepository.getAppointmentByIdAndPatientId(anyLong(), anyLong())).thenReturn(testAppointment);
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<PatientGetAppointmentResponse>> resultResponse =
                patientGetAppointmentById.getAppointment(testAppointment.getId(), testPatient.getId());

        assertThat(resultResponse.getBody()).isNotNull();

        PatientGetAppointmentResponse resultDto = resultResponse.getBody().getData();
        assertThat(resultDto)
                .usingRecursiveComparison()
                .isEqualTo(testResponse);

        verify(appointmentRepository).getAppointmentByIdAndPatientId(testAppointment.getId(), testPatient.getId());
        verify(objectStorageService).generateUrl(testAppointment.getDoctor().getProfilePicture());
    }
}
