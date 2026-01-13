package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetPatientByIdAppointments.class})
@ActiveProfiles("test")
class GetPatientByIdAppointmentsTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    PatientRepository patientRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @Autowired
    GetPatientByIdAppointments controller;

    @Test
    void getPatientAppointmentsShouldThrowApiNotFoundExceptionWhenPatientDoesNotExist() {
        Long patientId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        AppointmentStatus status = AppointmentStatus.FINISHED;

        when(patientRepository.existsById(patientId)).thenReturn(false);

        assertThatThrownBy(() -> controller.getPatientAppointments(patientId, pageable, status))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(patientRepository).existsById(patientId);
        verifyNoInteractions(appointmentRepository, objectStorageService);
    }

    @Test
    void getPatientAppointmentsShouldReturnMappedPageWhenPatientExists() {
        Long patientId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        AppointmentStatus status = AppointmentStatus.FINISHED;

        when(patientRepository.existsById(patientId)).thenReturn(true);

        String profilePictureKey = "doctors/mariusz.png";
        String imageUrl = "http://some-example.png.url";

        Patient patient = Patient.builder()
                .id(patientId)
                .name("Jan")
                .surname("Nowak")
                .build();

        Service service = Service.builder()
                .id(10L)
                .name("Konsultacja psychiatryczna (kolejna wizyta)")
                .price(BigDecimal.valueOf(10000))
                .build();

        Payment payment = Payment.builder()
                .id(20L)
                .amount(BigDecimal.valueOf(10000))
                .status(PaymentStatus.CREATED)
                .build();

        Specialization spec = Specialization.builder()
                .id(30L)
                .name("Psychiatra dorosłych")
                .build();

        Doctor doctor = Doctor.builder()
                .id(40L)
                .name("Mariusz")
                .surname("Mariuszowski")
                .profilePicture(profilePictureKey)
                .specializations(Set.of(spec))
                .build();

        LocalDateTime startedAt = LocalDateTime.of(2026, 1, 12, 10, 0);

        Appointment appointment = Appointment.builder()
                .id(50L)
                .status(status)
                .startedAt(startedAt)
                .description("Lęki")
                .doctor(doctor)
                .service(service)
                .payment(payment)
                .patient(patient)
                .build();

        PageImpl<Appointment> appointmentsPage = new PageImpl<>(List.of(appointment), pageable, 1);

        when(appointmentRepository.findAllByPatientIdAndStatus(patientId, status, pageable))
                .thenReturn(appointmentsPage);

        when(objectStorageService.generateUrl(profilePictureKey)).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<Page<GetPatientByIdAppointments.GetPatientByIdAppointmentResponse>>> responseEntity =
                controller.getPatientAppointments(patientId, pageable, status);

        assertThat(responseEntity.getBody()).isNotNull();

        Page<GetPatientByIdAppointments.GetPatientByIdAppointmentResponse> resultPage =
                responseEntity.getBody().getData();

        assertThat(resultPage.getContent()).hasSize(1);

        GetPatientByIdAppointments.GetPatientByIdAppointmentResponse dto =
                resultPage.getContent().get(0);

        assertThat(dto.id()).isEqualTo(50L);
        assertThat(dto.status()).isEqualTo(status);
        assertThat(dto.startedAt()).isEqualTo(startedAt);
        assertThat(dto.description()).isEqualTo("Lęki");

        assertThat(dto.patient().id()).isEqualTo(patientId);
        assertThat(dto.patient().name()).isEqualTo("Jan");
        assertThat(dto.patient().surname()).isEqualTo("Nowak");

        assertThat(dto.service().id()).isEqualTo(10L);
        assertThat(dto.service().name()).isEqualTo("Konsultacja psychiatryczna (kolejna wizyta)");
        assertThat(dto.service().price()).isEqualByComparingTo(BigDecimal.valueOf(10000));

        assertThat(dto.payment().id()).isEqualTo(20L);
        assertThat(dto.payment().amount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(dto.payment().status()).isEqualTo(PaymentStatus.CREATED);

        assertThat(dto.doctor().id()).isEqualTo(40L);
        assertThat(dto.doctor().name()).isEqualTo("Mariusz");
        assertThat(dto.doctor().surname()).isEqualTo("Mariuszowski");
        assertThat(dto.doctor().profilePicture()).isEqualTo(imageUrl);
        assertThat(dto.doctor().specializations())
                .extracting(GetPatientByIdAppointments.GetPatientByIdAppointmentResponse.DoctorDto.SpecializationDto::name)
                .containsExactly("Psychiatra dorosłych");

        verify(patientRepository).existsById(patientId);
        verify(appointmentRepository).findAllByPatientIdAndStatus(patientId, status, pageable);
        verify(objectStorageService).generateUrl(profilePictureKey);
        verifyNoMoreInteractions(patientRepository, appointmentRepository, objectStorageService);
    }
}
