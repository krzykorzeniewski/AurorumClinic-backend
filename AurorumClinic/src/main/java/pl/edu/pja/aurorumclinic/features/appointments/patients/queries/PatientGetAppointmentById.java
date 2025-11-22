package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientGetAppointmentById {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientGetAppointmentResponse>> getAppointment(@PathVariable("id") Long appointmentId,
                                                                                     @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(ApiResponse.success(handle(appointmentId, userId)));
    }

    private PatientGetAppointmentResponse handle(Long appointmentId, Long userId) {
        Appointment appointmentFromDb = appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId);
        if (appointmentFromDb == null) {
            throw new ApiNotFoundException("id not found", "id");
        }
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Appointment patient id does not match request patient id");
        }
        PatientGetAppointmentResponse response = PatientGetAppointmentResponse.builder()
                .id(appointmentFromDb.getId())
                .status(appointmentFromDb.getStatus())
                .startedAt(appointmentFromDb.getStartedAt())
                .description(appointmentFromDb.getDescription())
                .doctor(PatientGetAppointmentResponse.DoctorDto.builder()
                        .id(appointmentFromDb.getDoctor().getId())
                        .name(appointmentFromDb.getDoctor().getName())
                        .surname(appointmentFromDb.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(appointmentFromDb.getDoctor()
                                .getProfilePicture()))
                        .specializations(appointmentFromDb.getDoctor().getSpecializations()
                                .stream().map(spec -> PatientGetAppointmentResponse.
                                        DoctorDto.SpecializationDto.builder()
                                        .id(spec.getId())
                                        .name(spec.getName())
                                        .build()).toList())
                        .build())
                .service(PatientGetAppointmentResponse.ServiceDto.builder()
                        .id(appointmentFromDb.getService().getId())
                        .name(appointmentFromDb.getService().getName())
                        .price(appointmentFromDb.getService().getPrice())
                        .build())
                .payment(PatientGetAppointmentResponse.PaymentDto.builder()
                        .id(appointmentFromDb.getPayment().getId())
                        .amount(appointmentFromDb.getPayment().getAmount())
                        .status(appointmentFromDb.getPayment().getStatus())
                        .build())
                .build();
        return response;
    }

}
