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
    public ResponseEntity<?> updateAppointmentForUnregisteredUser() {
        return null;
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAppointmentForUnregisteredUser(@RequestParam("token") String token) {
        appointmentUnregisteredService.deleteAppointmentForUnregisteredUser(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
