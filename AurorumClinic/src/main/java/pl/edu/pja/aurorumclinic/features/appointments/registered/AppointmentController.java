package pl.edu.pja.aurorumclinic.features.appointments.registered;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/me")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentService.createAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateAppointment(@RequestBody @Valid UpdateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentService.updateAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAppointment(@RequestBody @Valid DeleteAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentService.deleteAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/me")
    public ResponseEntity<?> getAppointment(@PathVariable Long id,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointmentForPatient(id, userId)));
    }
}
