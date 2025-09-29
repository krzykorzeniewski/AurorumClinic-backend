package pl.edu.pja.aurorumclinic.features.appointments.patients;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(UserRole.PATIENT.name())")
public class AppointmentPatientController {

    private final AppointmentPatientService appointmentPatientService;

    @PostMapping("")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentPatientService.createAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@RequestBody @Valid UpdateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId,
                                               @PathVariable("id") Long appointmentId) {
        appointmentPatientService.updateAppointment(request, userId, appointmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long appointmentId,
                                               @AuthenticationPrincipal Long userId) {
        appointmentPatientService.deleteAppointment(appointmentId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointment(@PathVariable Long id,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentPatientService.getAppointmentForPatient(id, userId)));
    }
}
