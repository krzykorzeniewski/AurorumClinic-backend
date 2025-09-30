package pl.edu.pja.aurorumclinic.features.appointments.employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority(UserRole.EMPLOYEE.name())")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentRequest request) {
        appointmentService.createAppointment(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@RequestBody @Valid UpdateAppointmentRequest request,
                                               @PathVariable("id") Long appointmentId) {
        appointmentService.updateAppointment(appointmentId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
