package pl.edu.pja.aurorumclinic.features.appointments.registered;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/me")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentService.createAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}
