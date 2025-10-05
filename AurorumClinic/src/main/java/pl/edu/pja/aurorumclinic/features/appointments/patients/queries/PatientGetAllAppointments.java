package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientGetAllAppointments {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<PatientGetAppointmentResponse>>> getMyAppointments(
                                                                    @AuthenticationPrincipal Long patientId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(handle(patientId, page, size)));
    }

    private Page<PatientGetAppointmentResponse> handle(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PatientGetAppointmentResponse> response = appointmentRepository
                .getAllPatientAppointments(patientId, pageable);
        response.forEach(r -> {
            if (r.doctor().getProfilePicture() != null) {
                try {
                    r.doctor().setProfilePicture(objectStorageService.
                            generateSignedUrl(r.doctor().getProfilePicture()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return response;
    }

}
