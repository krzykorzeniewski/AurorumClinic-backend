package pl.edu.pja.aurorumclinic.features.schedules.queries;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetScheduleByIdAppointments {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/me/{id}/appointments")
    public ResponseEntity<ApiResponse<List<DoctorGetScheduleAppointmentResponse>>> docGetScheduleByIdAppointments(
            @PathVariable("id") Long scheduleId, @AuthenticationPrincipal Long doctorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(scheduleId, doctorId)));
    }

    private List<DoctorGetScheduleAppointmentResponse> handle(Long scheduleId, Long doctorId) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "doctorId")
        );
        scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "scheduleId")
        );
        List<Appointment> appointmentsFromSchedule = appointmentRepository.findByService_Schedules_Id(scheduleId);
        return appointmentsFromSchedule.stream().map(appointmentFromDb ->
                DoctorGetScheduleAppointmentResponse.builder()
                        .id(appointmentFromDb.getId())
                        .status(appointmentFromDb.getStatus())
                        .startedAt(appointmentFromDb.getStartedAt())
                        .description(appointmentFromDb.getDescription())
                        .service(DoctorGetScheduleAppointmentResponse.ServiceDto.builder()
                                .id(appointmentFromDb.getService().getId())
                                .name(appointmentFromDb.getService().getName())
                                .price(appointmentFromDb.getService().getPrice())
                                .build())
                        .payment(DoctorGetScheduleAppointmentResponse.PaymentDto.builder()
                                .id(appointmentFromDb.getPayment().getId())
                                .amount(appointmentFromDb.getPayment().getAmount())
                                .status(appointmentFromDb.getPayment().getStatus())
                                .build())
                        .patient(DoctorGetScheduleAppointmentResponse.PatientDto.builder()
                                .id(appointmentFromDb.getPatient().getId())
                                .name(appointmentFromDb.getPatient().getName())
                                .surname(appointmentFromDb.getPatient().getSurname())
                                .phoneNumber(appointmentFromDb.getPatient().getPhoneNumber())
                                .email(appointmentFromDb.getPatient().getEmail())
                                .build())
                        .build()).toList();
    }

    @Builder
    record DoctorGetScheduleAppointmentResponse(Long id,
                                                 @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                                 LocalDateTime startedAt,
                                                 String description,
                                                 ServiceDto service,
                                                 PaymentDto payment,
                                                 PatientDto patient) {
        @Builder
        record ServiceDto(Long id,
                          String name,
                          @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {
        }
        @Builder
        record PaymentDto(Long id,
                          @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal amount,
                          @JsonFormat(shape = JsonFormat.Shape.STRING) PaymentStatus status) {
        }
        @Builder
        record PatientDto(Long id,
                          String name,
                          String surname,
                          String phoneNumber,
                          String email) {
        }
    }
}
