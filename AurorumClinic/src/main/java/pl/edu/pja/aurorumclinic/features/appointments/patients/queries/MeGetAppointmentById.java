package pl.edu.pja.aurorumclinic.features.appointments.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.shared.dtos.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class MeGetAppointmentById {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetAppointmentResponse>> getAppointment(@PathVariable("id") Long appointmentId,
                                                                              @AuthenticationPrincipal Long userId) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(handle(appointmentId, userId)));
    }

    private GetAppointmentResponse handle(Long appointmentId, Long userId) {
        GetAppointmentResponse response = appointmentRepository.getAppointmentByPatientIdAndAppointmentId(userId, appointmentId);
        if (response == null) {
            throw new ApiNotFoundException("id not found", "id");
        }
        response.doctor().setProfilePicture(objectStorageService.generateUrl(response.doctor().getProfilePicture()));
        return response;
    }

}
