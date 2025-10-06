package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class MeCreateAppointment {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentValidator appointmentValidator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AppointmentRepository appointmentRepository;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createAppointment(@RequestBody @Valid PatientCreateAppointmentRequest request,
                                               @AuthenticationPrincipal Long userId) {
        handle(request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(PatientCreateAppointmentRequest request, Long userId) {
        Patient patientFromDb = patientRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Service serviceFromDb = serviceRepository.findById(request.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Appointment newAppointment = Appointment.builder()
                .service(serviceFromDb)
                .description(request.description())
                .patient(patientFromDb)
                .status(AppointmentStatus.CREATED)
                .doctor(doctorFromDb)
                .startedAt(request.startedAt())
                .finishedAt(request.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .build();
        appointmentValidator.validateTimeSlot(newAppointment.getStartedAt(), newAppointment.getFinishedAt(),
                newAppointment.getDoctor().getId(), newAppointment.getService().getId());

        Appointment appointmentFromDb = appointmentRepository.save(newAppointment);
        applicationEventPublisher.publishEvent(
                new AppointmentCreatedEvent(patientFromDb, appointmentFromDb));
    }

    public record PatientCreateAppointmentRequest(@NotNull LocalDateTime startedAt,
                                                   @NotNull Long serviceId,
                                                   @NotNull Long doctorId,
                                                   @NotBlank String description) {
    }

}
