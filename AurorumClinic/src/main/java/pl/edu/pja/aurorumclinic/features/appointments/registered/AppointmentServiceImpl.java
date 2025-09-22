package pl.edu.pja.aurorumclinic.features.appointments.registered;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService{

    private final AppointmentValidator appointmentValidator;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;
    private final AppointmentRepository appointmentRepository;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Override
    public void createAppointment(CreateAppointmentRequest createAppointmentRequest, Long userId) {
        Patient patientFromDb = (Patient) userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(createAppointmentRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Service serviceFromDb = serviceRepository.findById(createAppointmentRequest.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Appointment newAppointment = Appointment.builder()
                .service(serviceFromDb)
                .description(createAppointmentRequest.description())
                .patient(patientFromDb)
                .status(AppointmentStatus.CREATED)
                .doctor(doctorFromDb)
                .startedAt(createAppointmentRequest.startedAt())
                .finishedAt(createAppointmentRequest.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .build();
        appointmentValidator.validateTimeSlot(newAppointment.getStartedAt(), newAppointment.getFinishedAt(),
                newAppointment.getDoctor().getId(), newAppointment.getService().getId());
        appointmentRepository.save(newAppointment);

        sendConfirmationEmail(patientFromDb);
    }

    private void sendConfirmationEmail(Patient patientFromDb) {
        emailService.sendEmail(
                noreplyEmailAddres, patientFromDb.getEmail(),
                "wizyta umówiona", "twoja wizyta została umówiona");
    }
}
