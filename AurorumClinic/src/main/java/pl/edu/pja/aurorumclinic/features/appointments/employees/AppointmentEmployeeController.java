package pl.edu.pja.aurorumclinic.features.appointments.employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentEmployeeController {

    private final AppointmentEmployeeService appointmentEmployeeService;

    @PostMapping("")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentEmployeeRequest request) {
        appointmentEmployeeService.createAppointment(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@RequestBody @Valid UpdateAppointmentEmployeeRequest request,
                                               @PathVariable("id") Long appointmentId) {
        appointmentEmployeeService.updateAppointment(appointmentId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable("id") Long appointmentId) {
        appointmentEmployeeService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
