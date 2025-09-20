package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments/unregistered")
@RequiredArgsConstructor
public class AppointmentUnregisteredController {

    private final AppointmentUnregisteredService appointmentUnregisteredService;

    @PostMapping("")
    public ResponseEntity<?> createAppointmentForUnregisteredUser(
            @RequestBody @Valid CreateAppointmentUnregisteredRequest createRequest) {
        appointmentUnregisteredService.createAppointmentForUnregisteredUser(createRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("")
    public ResponseEntity<?> updateAppointmentForUnregisteredUser(
                                      @RequestBody @Valid RescheduleAppointmentUnregisteredRequest rescheduleRequest) {
        appointmentUnregisteredService.rescheduleAppointmentForUnregisteredUser(rescheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAppointmentForUnregisteredUser(
            @RequestBody @Valid DeleteAppointmentUnregisteredRequest deleteRequest) {
        appointmentUnregisteredService.deleteAppointmentForUnregisteredUser(deleteRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
