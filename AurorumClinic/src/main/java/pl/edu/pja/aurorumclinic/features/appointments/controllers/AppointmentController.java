package pl.edu.pja.aurorumclinic.features.appointments.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateAppointmentUnregisteredRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.AppointmentService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/unregistered-user")
    public ResponseEntity<?> createAppointmentForUnregisteredUser(
            @RequestBody @Valid CreateAppointmentUnregisteredRequest createRequest) {
        appointmentService.createAppointmentForUnregisteredUser(createRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/unregistered-user")
    public ResponseEntity<?> deleteAppointmentForUnregisteredUser(@RequestParam("token") String token) {
        appointmentService.deleteAppointmentForUnregisteredUser(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
