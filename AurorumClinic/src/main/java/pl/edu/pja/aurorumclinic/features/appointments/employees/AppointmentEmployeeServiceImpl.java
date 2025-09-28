package pl.edu.pja.aurorumclinic.features.appointments.employees;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentEmployeeServiceImpl implements AppointmentEmployeeService{

    private final AppointmentValidator appointmentValidator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${mail.frontend.appointment.delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.reschedule-link}")
    private String rescheduleAppointmentLink;

    @Override
    public void createAppointment(CreateAppointmentEmployeeRequest request) {
        Patient patientFromDb = (Patient) userRepository.findById(request.patientId()).orElseThrow(
                () ->  new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(request.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        pl.edu.pja.aurorumclinic.shared.data.models.Service serviceFromDb = serviceRepository.findById(
                request.serviceId()).orElseThrow(
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

        String rescheduleLink = rescheduleAppointmentLink + appointmentFromDb.getId();
        String deleteLink = deleteAppointmentLink + appointmentFromDb.getId();
        applicationEventPublisher.publishEvent(
                new AppointmentCreatedEvent(patientFromDb, appointmentFromDb, rescheduleLink, deleteLink));
    }

    @Override
    public void updateAppointment(Long appointmentId, UpdateAppointmentEmployeeRequest request) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        LocalDateTime newStartedAt = request.startedAt();
        LocalDateTime newFinishedAt = newStartedAt.plusMinutes(appointmentFromDb.getService().getDuration());
        appointmentValidator.validateTimeSlot(newStartedAt, newFinishedAt, appointmentFromDb.getDoctor().getId(),
                appointmentFromDb.getService().getId());

        appointmentFromDb.setStartedAt(newStartedAt);
        appointmentFromDb.setFinishedAt(newFinishedAt);
        appointmentFromDb.setDescription(request.description());

        String rescheduleLink = rescheduleAppointmentLink + appointmentFromDb.getId();
        String deleteLink = deleteAppointmentLink + appointmentFromDb.getId();
        applicationEventPublisher.publishEvent(new AppointmentRescheduledEvent(appointmentFromDb.getPatient(),
                rescheduleLink, deleteLink, appointmentFromDb));
    }

    @Override
    public void deleteAppointment(Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        appointmentRepository.delete(appointmentFromDb);
        applicationEventPublisher.publishEvent(
                new AppointmentDeletedEvent(appointmentFromDb.getPatient(), appointmentFromDb));
    }

}
