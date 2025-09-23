package pl.edu.pja.aurorumclinic.features.appointments.guests;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments/guest")
@RequiredArgsConstructor
public class AppointmentGuestController {

    private final AppointmentGuestService appointmentGuestService;

    @PostMapping("")
    public ResponseEntity<?> createAppointmentForUnregisteredUser(
            @RequestBody @Valid CreateAppointmentGuestRequest createRequest) {
        appointmentGuestService.createAppointmentForUnregisteredUser(createRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("")
    public ResponseEntity<?> updateAppointmentForUnregisteredUser(
                                      @RequestBody @Valid RescheduleAppointmentGuestRequest rescheduleRequest) {
        appointmentGuestService.rescheduleAppointmentForUnregisteredUser(rescheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAppointmentForUnregisteredUser(
            @RequestBody @Valid DeleteAppointmentGuestRequest deleteRequest) {
        appointmentGuestService.deleteAppointmentForUnregisteredUser(deleteRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
