package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class DeleteAppointment {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> deleteAppointment(@PathVariable("id") Long appointmentId) {
        handle(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (Objects.equals(appointmentFromDb.getStatus(),AppointmentStatus.FINISHED)) {
            throw new ApiException("Appointment has finished, unable to delete", "id");
        }
        appointmentRepository.delete(appointmentFromDb);
        applicationEventPublisher.publishEvent(
                new AppointmentDeletedEvent(appointmentFromDb.getPatient(), appointmentFromDb));
    }

}
