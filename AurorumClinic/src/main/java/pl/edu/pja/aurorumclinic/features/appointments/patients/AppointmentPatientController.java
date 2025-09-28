package pl.edu.pja.aurorumclinic.features.appointments.patients;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments/patient")
@RequiredArgsConstructor
public class AppointmentPatientController {

    private final AppointmentPatientService appointmentPatientService;

    @PostMapping("/me")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentPatientService.createAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateAppointment(@RequestBody @Valid UpdateAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentPatientService.updateAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAppointment(@RequestBody @Valid DeleteAppointmentPatientRequest request,
                                               @AuthenticationPrincipal Long userId) {
        appointmentPatientService.deleteAppointment(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/me")
    public ResponseEntity<?> getAppointment(@PathVariable Long id,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(appointmentPatientService.getAppointmentForPatient(id, userId)));
    }
}
