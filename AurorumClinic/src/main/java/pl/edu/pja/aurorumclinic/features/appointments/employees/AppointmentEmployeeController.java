package pl.edu.pja.aurorumclinic.features.appointments.employees;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments/employee")
@RequiredArgsConstructor
public class AppointmentEmployeeController {

    private final AppointmentEmployeeService appointmentEmployeeService;

    @PostMapping("")
    public ResponseEntity<?> createAppointment(@RequestBody @Valid CreateAppointmentEmployeeRequest request) {
        appointmentEmployeeService.createAppointment(request);
        return null;
    }

}
