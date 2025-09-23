package pl.edu.pja.aurorumclinic.features.appointments.registered;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.response.GetAppointmentPatientResponse;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService{

    private final AppointmentValidator appointmentValidator;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${mail.frontend.appointment.delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.reschedule-link}")
    private String rescheduleAppointmentLink;

    @Override
    public void createAppointment(CreateAppointmentPatientRequest createAppointmentPatientRequest, Long userId) {
        Patient patientFromDb = (Patient) userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(createAppointmentPatientRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Service serviceFromDb = serviceRepository.findById(createAppointmentPatientRequest.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Appointment newAppointment = Appointment.builder()
                .service(serviceFromDb)
                .description(createAppointmentPatientRequest.description())
                .patient(patientFromDb)
                .status(AppointmentStatus.CREATED)
                .doctor(doctorFromDb)
                .startedAt(createAppointmentPatientRequest.startedAt())
                .finishedAt(createAppointmentPatientRequest.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .build();
        appointmentValidator.validateTimeSlot(newAppointment.getStartedAt(), newAppointment.getFinishedAt(),
                newAppointment.getDoctor().getId(), newAppointment.getService().getId());

        Appointment appointmentFromDb = appointmentRepository.save(newAppointment);

        String rescheduleLink = rescheduleAppointmentLink + appointmentFromDb.getId();
        String deleteLink = deleteAppointmentLink + appointmentFromDb.getId();
        applicationEventPublisher.publishEvent(
                new AppointmentCreatedEvent(patientFromDb, appointmentFromDb, rescheduleLink, deleteLink));
    }

    @Override
    public void updateAppointment(UpdateAppointmentPatientRequest updateAppointmentPatientRequest, Long userId) {
        Appointment appointmentFromDb = appointmentRepository.findById(updateAppointmentPatientRequest.appointmentId())
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Patient id does not match user id");
        }
        LocalDateTime newStartedAt = updateAppointmentPatientRequest.startedAt();
        LocalDateTime newFinishedAt = newStartedAt.plusMinutes(appointmentFromDb.getService().getDuration());
        appointmentValidator.validateTimeSlot(newStartedAt, newFinishedAt, appointmentFromDb.getDoctor().getId(),
                appointmentFromDb.getService().getId());

        appointmentFromDb.setStartedAt(newStartedAt);
        appointmentFromDb.setFinishedAt(newFinishedAt);
        appointmentFromDb.setDescription(updateAppointmentPatientRequest.description());

        String rescheduleLink = rescheduleAppointmentLink + appointmentFromDb.getId();
        String deleteLink = deleteAppointmentLink + appointmentFromDb.getId();
        applicationEventPublisher.publishEvent(new AppointmentRescheduledEvent(appointmentFromDb.getPatient(),
                rescheduleLink, deleteLink, appointmentFromDb));
    }

    @Override
    public void deleteAppointment(DeleteAppointmentPatientRequest deleteAppointmentPatientRequest, Long userId) {
        Appointment appointmentFromDb = appointmentRepository.findById(deleteAppointmentPatientRequest.appointmentId())
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Patient id does not match user id");
        }
        appointmentRepository.delete(appointmentFromDb);
        applicationEventPublisher.publishEvent(
                new AppointmentDeletedEvent(appointmentFromDb.getPatient(), appointmentFromDb));
    }

    @Override
    public GetAppointmentPatientResponse getAppointmentForPatient(Long appointmentId, Long userId) {
        GetAppointmentPatientResponse response = appointmentRepository.findByIdAndPatientId(appointmentId, userId);
        if (response == null) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        return response;
    }

}
