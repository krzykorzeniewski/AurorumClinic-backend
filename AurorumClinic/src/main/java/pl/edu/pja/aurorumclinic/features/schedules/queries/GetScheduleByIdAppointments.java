package pl.edu.pja.aurorumclinic.features.schedules.queries;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class GetScheduleByIdAppointments { //TODO rozdzielić

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}/appointments")
    public ResponseEntity<ApiResponse<Page<GetAppointmentResponse>>> getScheduleByIdAppointments(
            @PageableDefault Pageable pageable, @PathVariable("id") Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable, scheduleId)));
    }

    private Page<GetAppointmentResponse> handle(Pageable pageable, Long scheduleId) {
        scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Page<Appointment> appointmentsFromSchedule = appointmentRepository.findByService_Schedules_Id(pageable, scheduleId);
        Page<GetAppointmentResponse> response = appointmentsFromSchedule.map(appointmentFromDb ->
                GetAppointmentResponse.builder()
                        .id(appointmentFromDb.getId())
                        .status(appointmentFromDb.getStatus())
                        .startedAt(appointmentFromDb.getStartedAt())
                        .description(appointmentFromDb.getDescription())
                        .doctor(GetAppointmentResponse.DoctorDto.builder()
                                .id(appointmentFromDb.getDoctor().getId())
                                .name(appointmentFromDb.getDoctor().getName())
                                .surname(appointmentFromDb.getDoctor().getSurname())
                                .profilePicture(objectStorageService.generateUrl(appointmentFromDb.getDoctor()
                                        .getProfilePicture()))
                                .specializations(appointmentFromDb.getDoctor().getSpecializations()
                                        .stream().map(spec -> GetAppointmentResponse.
                                                DoctorDto.SpecializationDto.builder()
                                                .id(spec.getId())
                                                .name(spec.getName())
                                                .build()).toList())
                                .build())
                        .service(GetAppointmentResponse.ServiceDto.builder()
                                .id(appointmentFromDb.getService().getId())
                                .name(appointmentFromDb.getService().getName())
                                .price(appointmentFromDb.getService().getPrice())
                                .build())
                        .payment(GetAppointmentResponse.PaymentDto.builder()
                                .id(appointmentFromDb.getPayment().getId())
                                .amount(appointmentFromDb.getPayment().getAmount())
                                .status(appointmentFromDb.getPayment().getStatus())
                                .build())
                        .patient(GetAppointmentResponse.PatientDto.builder()
                                .id(appointmentFromDb.getPatient().getId())
                                .name(appointmentFromDb.getPatient().getName())
                                .surname(appointmentFromDb.getPatient().getSurname())
                                .phoneNumber(appointmentFromDb.getPatient().getPhoneNumber())
                                .email(appointmentFromDb.getPatient().getEmail())
                                .build())
                        .build());
        return response;
    }

    @Builder
     record GetAppointmentResponse(Long id,
                                         @JsonFormat(shape = JsonFormat.Shape.STRING) AppointmentStatus status,
                                         LocalDateTime startedAt,
                                         String description,
                                         DoctorDto doctor,
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
        @Builder
        record DoctorDto(Long id,
                                String name,
                                String surname,
                                String profilePicture,
                                List<SpecializationDto> specializations) {
            @Builder
            record SpecializationDto(Long id,
                                            String name) {

            }
        }
    }
}
