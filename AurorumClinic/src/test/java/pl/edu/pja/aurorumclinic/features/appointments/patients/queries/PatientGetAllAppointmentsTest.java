package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PatientGetAllAppointments.class}) //looks for all controllers which is actually so sigma with our queries/handlers approach;)
@ActiveProfiles("test")
public class PatientGetAllAppointmentsTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    PatientGetAllAppointments patientGetAllAppointments;

    @Test
    void shouldReturnPageOfDtosWithCorrectSizeForPatientId() {
        Patient testPatient = Patient.builder()
                .id(1L)
                .build();
        Pageable pageable = PageRequest.of(0, 5);
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
        String imageUrl = "http://some-example.png.url";
        PageImpl<Appointment> appointmentsPage = new PageImpl<>(List.of(testAppointment), pageable, 1);

        when(appointmentRepository.findAllByPatientId(anyLong(), any())).thenReturn(appointmentsPage);
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<Page<PatientGetAppointmentResponse>>> resultResponse =
                patientGetAllAppointments.getMyAppointments(testPatient.getId(), pageable);

        assertThat(resultResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(resultResponse.getBody()).isNotNull();

        Page<PatientGetAppointmentResponse> resultPage = resultResponse.getBody().getData();
        assertThat(resultPage.getContent()).hasSize(1);

        PatientGetAppointmentResponse resultDto = resultPage.getContent().get(0);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.id()).isEqualTo(testAppointment.getId());
        assertThat(resultDto.status()).isEqualTo(testAppointment.getStatus());
        assertThat(resultDto.startedAt()).isEqualTo(testAppointment.getStartedAt());

        assertThat(resultDto.doctor().id()).isEqualTo(testAppointment.getDoctor().getId());
        assertThat(resultDto.doctor().name()).isEqualTo(testAppointment.getDoctor().getName());
        assertThat(resultDto.doctor().specializations()).hasSize(1);
        assertThat(resultDto.doctor().profilePicture()).isEqualTo(imageUrl);

        assertThat(resultDto.service().id()).isEqualTo(testAppointment.getService().getId());
        assertThat(resultDto.service().name()).isEqualTo(testAppointment.getService().getName());

        assertThat(resultDto.payment().id()).isEqualTo(testAppointment.getPayment().getId());
        assertThat(resultDto.payment().status()).isEqualTo(testAppointment.getPayment().getStatus());

        verify(appointmentRepository).findAllByPatientId(testPatient.getId(), pageable);
        verify(objectStorageService).generateUrl(testAppointment.getDoctor().getProfilePicture());
    }

}
