package pl.edu.pja.aurorumclinic.features.appointments.employees.queries;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
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
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {EmployeeGetAllAppointments.class})
@ActiveProfiles("test")
public class EmployeeGetAllAppointmentsTest {

    @MockitoBean
    AppointmentRepository appointmentRepository;

    @MockitoBean
    ObjectStorageService objectStorageService;

    @MockitoBean
    UserRepository userRepository;

    @Autowired
    EmployeeGetAllAppointments employeeGetAllAppointments;

    @Test
    void getAllAppointmentsShouldThrowApiNotFoundExceptionWhenEmpIdIsNotFound() {
        Pageable pageable = PageRequest.of(0, 5);
        Long empId = 1L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeGetAllAppointments.getAllAppointments(pageable, empId))
                .isExactlyInstanceOf(ApiNotFoundException.class);
        verify(userRepository).findById(empId);
    }

    @Test
    void getAllAppointmentsShouldReturnAppointmentsForDoctorIdIfEmpRoleIsDoctor() {
        Pageable pageable = PageRequest.of(0, 5);
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
        Appointment testAppointment1 = Appointment.builder()
                .id(1L)
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
                .id(testAppointment1.getId())
                .status(testAppointment1.getStatus())
                .description(testAppointment1.getDescription())
                .startedAt(testAppointment1.getStartedAt())
                .doctor(GetAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment1.getDoctor().getId())
                        .name(testAppointment1.getDoctor().getName())
                        .surname(testAppointment1.getDoctor().getSurname())
                        .profilePicture(imageUrl)
                        .specializations(testAppointment1.getDoctor().getSpecializations()
                                .stream().map(spec -> GetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment1.getService().getId())
                        .name(testAppointment1.getService().getName())
                        .price(testAppointment1.getService().getPrice())
                        .build())
                .payment(GetAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment1.getPayment().getId())
                        .amount(testAppointment1.getPayment().getAmount())
                        .status(testAppointment1.getPayment().getStatus())
                        .build())
                .patient(GetAppointmentResponse.PatientDto.builder()
                        .id(testAppointment1.getPatient().getId())
                        .name(testAppointment1.getPatient().getName())
                        .surname(testAppointment1.getPatient().getSurname())
                        .build())
                .build();
        PageImpl<Appointment> appointments = new PageImpl<>(List.of(testAppointment1), pageable, 1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findAllByDoctorId(any(), anyLong())).thenReturn(appointments);
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<Page<GetAppointmentResponse>>> resultResponse =
                employeeGetAllAppointments.getAllAppointments(pageable, empId);

        assertThat(resultResponse.getBody()).isNotNull();

        Page<GetAppointmentResponse> resultPage = resultResponse.getBody().getData();
        assertThat(resultPage).hasSize(1);

        GetAppointmentResponse resultDto = resultPage.getContent().get(0);

        assertThat(resultDto)
                .isEqualTo(testResponse);
        verify(appointmentRepository).findAllByDoctorId(pageable, empId);
        verify(objectStorageService).generateUrl(testDoctor.getProfilePicture());
    }

    @Test
    void getAllAppointmentsShouldReturnAllAppointmentsIfEmpRoleIsEmployee() {
        Pageable pageable = PageRequest.of(0, 5);
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
        Appointment testAppointment1 = Appointment.builder()
                .id(1L)
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
                .id(testAppointment1.getId())
                .status(testAppointment1.getStatus())
                .description(testAppointment1.getDescription())
                .startedAt(testAppointment1.getStartedAt())
                .doctor(GetAppointmentResponse.DoctorDto.builder()
                        .id(testAppointment1.getDoctor().getId())
                        .name(testAppointment1.getDoctor().getName())
                        .surname(testAppointment1.getDoctor().getSurname())
                        .profilePicture(imageUrl)
                        .specializations(testAppointment1.getDoctor().getSpecializations()
                                .stream().map(spec -> GetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetAppointmentResponse.ServiceDto.builder()
                        .id(testAppointment1.getService().getId())
                        .name(testAppointment1.getService().getName())
                        .price(testAppointment1.getService().getPrice())
                        .build())
                .payment(GetAppointmentResponse.PaymentDto.builder()
                        .id(testAppointment1.getPayment().getId())
                        .amount(testAppointment1.getPayment().getAmount())
                        .status(testAppointment1.getPayment().getStatus())
                        .build())
                .patient(GetAppointmentResponse.PatientDto.builder()
                        .id(testAppointment1.getPatient().getId())
                        .name(testAppointment1.getPatient().getName())
                        .surname(testAppointment1.getPatient().getSurname())
                        .build())
                .build();
        PageImpl<Appointment> appointments = new PageImpl<>(List.of(testAppointment1), pageable, 1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(appointmentRepository.findAll(any(Pageable.class))).thenReturn(appointments);
        when(objectStorageService.generateUrl(anyString())).thenReturn(imageUrl);

        ResponseEntity<ApiResponse<Page<GetAppointmentResponse>>> resultResponse =
                employeeGetAllAppointments.getAllAppointments(pageable, empId);

        assertThat(resultResponse.getBody()).isNotNull();

        Page<GetAppointmentResponse> resultPage = resultResponse.getBody().getData();
        assertThat(resultPage).hasSize(1);

        GetAppointmentResponse resultDto = resultPage.getContent().get(0);

        assertThat(resultDto)
                .isEqualTo(testResponse);
        verify(appointmentRepository).findAll(pageable);
        verify(objectStorageService).generateUrl(testDoctor.getProfilePicture());
    }

}
