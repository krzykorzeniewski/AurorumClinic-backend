package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.MeGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class MeGetAllAppointments {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<MeGetAppointmentResponse>>> getMyAppointments(
                                                            @AuthenticationPrincipal Long patientId,
                                                            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(patientId, pageable)));
    }

    private Page<MeGetAppointmentResponse> handle(Long patientId, Pageable pageable) {
        Page<Appointment> appointmentsFromDb = appointmentRepository.findAllByPatientId(patientId, pageable);
        Page<MeGetAppointmentResponse> response = appointmentsFromDb.map(appointmentFromDb -> MeGetAppointmentResponse.builder()
                .id(appointmentFromDb.getId())
                .status(appointmentFromDb.getStatus())
                .startedAt(appointmentFromDb.getStartedAt())
                .description(appointmentFromDb.getDescription())
                .doctor(MeGetAppointmentResponse.DoctorDto.builder()
                        .id(appointmentFromDb.getDoctor().getId())
                        .name(appointmentFromDb.getDoctor().getName())
                        .surname(appointmentFromDb.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(appointmentFromDb.getDoctor()
                                .getProfilePicture()))
                        .specializations(appointmentFromDb.getDoctor().getSpecializations()
                                .stream().map(spec -> MeGetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(MeGetAppointmentResponse.ServiceDto.builder()
                        .id(appointmentFromDb.getService().getId())
                        .name(appointmentFromDb.getService().getName())
                        .price(appointmentFromDb.getService().getPrice())
                        .build())
                .payment(MeGetAppointmentResponse.PaymentDto.builder()
                        .id(appointmentFromDb.getPayment().getId())
                        .amount(appointmentFromDb.getPayment().getAmount())
                        .status(appointmentFromDb.getPayment().getStatus())
                        .build())
                .build());
        return response;
    }

}
