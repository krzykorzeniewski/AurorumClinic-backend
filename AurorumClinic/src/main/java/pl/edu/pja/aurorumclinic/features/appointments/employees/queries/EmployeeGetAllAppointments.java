package pl.edu.pja.aurorumclinic.features.appointments.employees.queries;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.employees.queries.shared.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.util.Objects;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class EmployeeGetAllAppointments {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;
    private final UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetAppointmentResponse>>> getAllAppointments(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) LocalDate date,
            @AuthenticationPrincipal Long empId) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable, empId, date)));
    }

    private Page<GetAppointmentResponse> handle(Pageable pageable, Long empId, LocalDate date) {
        Page<Appointment> appointmentsFromDb;
        User empFromDb = userRepository.findById(empId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (Objects.equals(empFromDb.getRole(), UserRole.DOCTOR)) {
            if (date == null) {
                appointmentsFromDb = appointmentRepository.findAllByDoctorId(pageable, empId);
            } else {
                appointmentsFromDb = appointmentRepository.findAllByDoctorIdAndStartedAtEquals(pageable, empId, date);
            }
        } else {
            if (date == null) {
                appointmentsFromDb = appointmentRepository.findAll(pageable);
            } else {
                appointmentsFromDb = appointmentRepository.findAllByDate(pageable, date);
            }
        }
        return appointmentsFromDb.map(appointment -> GetAppointmentResponse.builder()
                .id(appointment.getId())
                .status(appointment.getStatus())
                .description(appointment.getDescription())
                .startedAt(appointment.getStartedAt())
                .doctor(GetAppointmentResponse.DoctorDto.builder()
                        .id(appointment.getDoctor().getId())
                        .name(appointment.getDoctor().getName())
                        .surname(appointment.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(appointment.getDoctor()
                                .getProfilePicture()))
                        .specializations(appointment.getDoctor().getSpecializations()
                                .stream().map(spec -> GetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetAppointmentResponse.ServiceDto.builder()
                        .id(appointment.getService().getId())
                        .name(appointment.getService().getName())
                        .price(appointment.getService().getPrice())
                        .build())
                .payment(GetAppointmentResponse.PaymentDto.builder()
                        .id(appointment.getPayment().getId())
                        .amount(appointment.getPayment().getAmount())
                        .status(appointment.getPayment().getStatus())
                        .build())
                .patient(GetAppointmentResponse.PatientDto.builder()
                        .id(appointment.getPatient().getId())
                        .name(appointment.getPatient().getName())
                        .surname(appointment.getPatient().getSurname())
                        .build())
                .build());
    }

}

