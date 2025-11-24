package pl.edu.pja.aurorumclinic.features.appointments.employees.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.appointments.employees.queries.shared.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GetAppointmentById.class})
@ActiveProfiles("test")
public class GetAppointmentByIdTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    GetAppointmentById getAppointmentById;

    @Test
    void getAppointmentByIdShouldThrowApiNotFoundExceptionWhenEmpIdNotFound() {
        Long appointmentId = 1L;
        Long empId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAppointmentById.getAppointmentById(appointmentId, empId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(userRepository).findById(empId);
    }

    @Test
    void getAppointmentByIdShouldThrowApiNotFoundExceptionWhenAppointmentIdNotFound() {
        Long appointmentId = 1L;
        Long empId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(User.builder()
                        .id(empId)
                .build()));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAppointmentById.getAppointmentById(appointmentId, empId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(userRepository).findById(empId);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void getAppointmentByIdShouldThrowApiAuthExceptionWhenUserHasRoleDoctorAndIsNotAppointmentsDoctor() {
        Long appointmentId = 1L;
        Long empId = 1L;

        Doctor testDoctor = Doctor.builder()
                .id(empId)
                .role(UserRole.DOCTOR)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .doctor(Doctor.builder()
                        .id(2L)
                        .build())
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));

        assertThatThrownBy(() -> getAppointmentById.getAppointmentById(appointmentId, empId))
                .isExactlyInstanceOf(ApiAuthorizationException.class);
        verify(userRepository).findById(empId);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void getAppointmentByIdShouldReturnValidDtoWhenUserRoleIsEmployee() {
        Long appointmentId = 1L;
        Long empId = 1L;

        User testEmployee = User.builder()
                .id(empId)
                .role(UserRole.EMPLOYEE)
                .build();
        Doctor testDoctor = Doctor.builder()
                .id(2L)
                .name("Mariusz")
                .surname("Kowalski")
                .profilePicture("example.png")
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build(),
                        Specialization.builder()
                                .id(2L)
                                .name("Psychiatra dziecięcy")
                                .build()
                ))
                .role(UserRole.DOCTOR)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .doctor(testDoctor)
                .service(Service.builder()
                        .id(1L)
                        .name("Konsultacja psychiatryczna dorosłych (kolejna wizyta)")
                        .duration(30)
                        .price(BigDecimal.valueOf(1000000))
                        .build())
                .payment(Payment.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(1000000))
                        .status(PaymentStatus.COMPLETED)
                        .build())
                .patient(Patient.builder()
                        .id(1L)
                        .name("Mariusz")
                        .build())
                .build();
        String imageUrl = "http://example.png";
        GetAppointmentResponse testResponse = GetAppointmentResponse.builder()
                .id(testAppointment.getId())
                .status(testAppointment.getStatus())
                .description(testAppointment.getDescription())
                .startedAt(testAppointment.getStartedAt())
                .doctor(GetAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment.getDoctor().getId())
                        .name(testAppointment.getDoctor().getName())
                        .surname(testAppointment.getDoctor().getSurname())
                        .profilePicture(imageUrl)
                        .specializations(testAppointment.getDoctor().getSpecializations()
                                .stream().map(spec -> GetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment.getService().getId())
                        .name(testAppointment.getService().getName())
                        .price(testAppointment.getService().getPrice())
                        .build())
                .payment(GetAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment.getPayment().getId())
                        .amount(testAppointment.getPayment().getAmount())
                        .status(testAppointment.getPayment().getStatus())
                        .build())
                .patient(GetAppointmentResponse.PatientDto.builder()
                        .id(testAppointment.getPatient().getId())
                        .name(testAppointment.getPatient().getName())
                        .surname(testAppointment.getPatient().getSurname())
                        .build())
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<GetAppointmentResponse>> resultResponse =
                getAppointmentById.getAppointmentById(appointmentId, empId);

        assertThat(resultResponse.getBody()).isNotNull();

        GetAppointmentResponse resultDto = resultResponse.getBody().getData();

        assertThat(resultDto)
                .isEqualTo(testResponse);
        verify(userRepository).findById(empId);
        verify(appointmentRepository).findById(appointmentId);
        verify(objectStorageService).generateUrl(testDoctor.getProfilePicture());
    }

    @Test
    void getAppointmentByIdShouldReturnValidDtoWhenUserRoleIsDoctor() {
        Long appointmentId = 1L;
        Long empId = 1L;
        Doctor testDoctor = Doctor.builder()
                .id(empId)
                .name("Mariusz")
                .surname("Kowalski")
                .profilePicture("example.png")
                .specializations(Set.of(
                        Specialization.builder()
                                .id(1L)
                                .name("Psychiatra dorosłych")
                                .build(),
                        Specialization.builder()
                                .id(2L)
                                .name("Psychiatra dziecięcy")
                                .build()
                ))
                .role(UserRole.DOCTOR)
                .build();
        Appointment testAppointment = Appointment.builder()
                .id(appointmentId)
                .doctor(testDoctor)
                .service(Service.builder()
                        .id(1L)
                        .name("Konsultacja psychiatryczna dorosłych (kolejna wizyta)")
                        .duration(30)
                        .price(BigDecimal.valueOf(1000000))
                        .build())
                .payment(Payment.builder()
                        .id(1L)
                        .amount(BigDecimal.valueOf(1000000))
                        .status(PaymentStatus.COMPLETED)
                        .build())
                .patient(Patient.builder()
                        .id(1L)
                        .name("Mariusz")
                        .build())
                .build();
        String imageUrl = "http://example.png";
        GetAppointmentResponse testResponse = GetAppointmentResponse.builder()
                .id(testAppointment.getId())
                .status(testAppointment.getStatus())
                .description(testAppointment.getDescription())
                .startedAt(testAppointment.getStartedAt())
                .doctor(GetAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment.getDoctor().getId())
                        .name(testAppointment.getDoctor().getName())
                        .surname(testAppointment.getDoctor().getSurname())
                        .profilePicture(imageUrl)
                        .specializations(testAppointment.getDoctor().getSpecializations()
                                .stream().map(spec -> GetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment.getService().getId())
                        .name(testAppointment.getService().getName())
                        .price(testAppointment.getService().getPrice())
                        .build())
                .payment(GetAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment.getPayment().getId())
                        .amount(testAppointment.getPayment().getAmount())
                        .status(testAppointment.getPayment().getStatus())
                        .build())
                .patient(GetAppointmentResponse.PatientDto.builder()
                        .id(testAppointment.getPatient().getId())
                        .name(testAppointment.getPatient().getName())
                        .surname(testAppointment.getPatient().getSurname())
                        .build())
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(testAppointment));
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<GetAppointmentResponse>> resultResponse =
                getAppointmentById.getAppointmentById(appointmentId, empId);

        assertThat(resultResponse.getBody()).isNotNull();

        GetAppointmentResponse resultDto = resultResponse.getBody().getData();

        assertThat(resultDto)
                .isEqualTo(testResponse);
        verify(userRepository).findById(empId);
        verify(appointmentRepository).findById(appointmentId);
        verify(objectStorageService).generateUrl(testDoctor.getProfilePicture());
    }


}
