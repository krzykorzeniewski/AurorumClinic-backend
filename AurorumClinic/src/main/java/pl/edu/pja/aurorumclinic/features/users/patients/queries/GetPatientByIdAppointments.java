package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.employees.queries.shared.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.MeGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class GetPatientByIdAppointments {

    private final PatientRepository patientRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<GetPatientByIdAppointmentResponse>>> getPatientAppointments(
                                                                             @PathVariable("id") Long patientId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(handle(patientId, page, size)));
    }

    private Page<GetPatientByIdAppointmentResponse> handle(Long patientId, int page, int size) {
        if (!patientRepository.existsById(patientId)) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentsFromDb = patientRepository
                .getPatientAppointmentsById(patientId, pageable);
        Page<GetPatientByIdAppointmentResponse> response = appointmentsFromDb.map(appointmentFromDb ->
                GetPatientByIdAppointmentResponse.builder()
                .id(appointmentFromDb.getId())
                .status(appointmentFromDb.getStatus())
                .startedAt(appointmentFromDb.getStartedAt())
                .description(appointmentFromDb.getDescription())
                .doctor(GetPatientByIdAppointmentResponse.DoctorDto.builder()
                        .id(appointmentFromDb.getDoctor().getId())
                        .name(appointmentFromDb.getDoctor().getName())
                        .surname(appointmentFromDb.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(appointmentFromDb.getDoctor()
                                .getProfilePicture()))
                        .specializations(appointmentFromDb.getDoctor().getSpecializations()
                                .stream().map(spec -> GetPatientByIdAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(GetPatientByIdAppointmentResponse.ServiceDto.builder()
                        .id(appointmentFromDb.getService().getId())
                        .name(appointmentFromDb.getService().getName())
                        .price(appointmentFromDb.getService().getPrice())
                        .build())
                .payment(GetPatientByIdAppointmentResponse.PaymentDto.builder()
                        .id(appointmentFromDb.getPayment().getId())
                        .amount(appointmentFromDb.getPayment().getAmount())
                        .status(appointmentFromDb.getPayment().getStatus())
                        .build())
                .patient(GetPatientByIdAppointmentResponse.PatientDto.builder()
                        .id(appointmentFromDb.getPatient().getId())
                        .name(appointmentFromDb.getPatient().getName())
                        .surname(appointmentFromDb.getPatient().getSurname())
                        .build())
                .build());
        return response;
    }

    @Builder
    public record GetPatientByIdAppointmentResponse(Long id,
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
                                 String surname) {
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
