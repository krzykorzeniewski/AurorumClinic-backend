package pl.edu.pja.aurorumclinic.features.appointments.employees.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class DeleteAppointmentBulk {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @DeleteMapping("/bulk")
    @Transactional
    public ResponseEntity<ApiResponse<?>> deleteAppointmentsInBulk(
            @RequestBody @Valid DeleteAppointmentBulkRequest request) {
        handle(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(DeleteAppointmentBulkRequest request) {
        List<Appointment> appointmentsFromDb = appointmentRepository.findAllById(request.appointmentIds);
        if (appointmentsFromDb.size() != request.appointmentIds.size()) {
            throw new ApiNotFoundException("Some appointment ids are not found", "appointmentIds");
        }
        if (appointmentsFromDb.stream().anyMatch(
                appointment -> Objects.equals(appointment.getStatus(), AppointmentStatus.FINISHED))) {
            throw new ApiException("Appointment has finished, unable to delete", "id");
        }
        appointmentRepository.deleteAllInBatch(appointmentsFromDb);
        for (Appointment deletedAppointment : appointmentsFromDb) {
            applicationEventPublisher.publishEvent(new AppointmentDeletedEvent(
                    deletedAppointment.getPatient(), deletedAppointment));
        }
    }

    record DeleteAppointmentBulkRequest(@NotEmpty List<Long> appointmentIds) {

    }

}
