package pl.edu.pja.aurorumclinic.features.appointments.employees.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.employees.queries.shared.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class GetAppointmentById {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetAppointmentResponse>> getAppointmentById(
            @PathVariable("id") Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(handle(appointmentId)));
    }

    private GetAppointmentResponse handle(Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        GetAppointmentResponse response = GetAppointmentResponse.builder()
                .id(appointmentFromDb.getId())
                .status(appointmentFromDb.getStatus())
                .description(appointmentFromDb.getDescription())
                .startedAt(appointmentFromDb.getStartedAt())
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
                        .build())
                .build();
        return response;
    }

}
