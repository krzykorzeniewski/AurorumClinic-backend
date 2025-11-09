package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class RescheduleAppointment {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentValidator appointmentValidator;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateAppointment(@RequestBody @Valid EmployeeUpdateAppointmentRequest request,
                                               @PathVariable("id") Long appointmentId) {
        handle(request, appointmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(EmployeeUpdateAppointmentRequest request, Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        LocalDateTime newStartedAt = request.startedAt();
        LocalDateTime newFinishedAt = newStartedAt.plusMinutes(appointmentFromDb.getService().getDuration());
        appointmentValidator.validateRescheduledAppointment(newStartedAt, newFinishedAt, appointmentFromDb.getDoctor(),
                appointmentFromDb.getService(), appointmentFromDb);

        appointmentFromDb.setStartedAt(newStartedAt);
        appointmentFromDb.setFinishedAt(newFinishedAt);
        appointmentFromDb.setDescription(request.description());

        applicationEventPublisher.publishEvent(new AppointmentRescheduledEvent(appointmentFromDb.getPatient(),
                appointmentFromDb));
    }

    public record EmployeeUpdateAppointmentRequest(@NotNull LocalDateTime startedAt,
                                                   @Size(max = 500) String description) {
    }

}
