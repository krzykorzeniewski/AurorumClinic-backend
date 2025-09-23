package pl.edu.pja.aurorumclinic.features.appointments.registered;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.time.LocalDateTime;
import java.util.Objects;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService{

    private final AppointmentValidator appointmentValidator;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;
    private final AppointmentRepository appointmentRepository;
    private final SmsService smsService;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${twilio.trial_number}")
    private String clinicPhoneNumber;

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
        sendConfirmationNotification(patientFromDb, appointmentFromDb);
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
        sendConfirmationNotification(appointmentFromDb.getPatient(), appointmentFromDb);
    }

    @Override
    public void deleteAppointment(DeleteAppointmentPatientRequest deleteAppointmentPatientRequest, Long userId) {
        Appointment appointmentFromDb = appointmentRepository.findById(deleteAppointmentPatientRequest.appointmentId())
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Patient id does not match user id");
        }
        appointmentRepository.delete(appointmentFromDb);
        sendDeleteNotification(appointmentFromDb.getPatient());
    }

    private void sendConfirmationNotification(Patient patient, Appointment appointment) {
        String rescheduleLink = rescheduleAppointmentLink + appointment.getId();
        String deleteLink = deleteAppointmentLink + appointment.getId();
        String message = "twoja wizyta została umówiona \n" +
                "aby ją odwołać naciśnij link: " + deleteLink + "\n" +
                "aby ją przełożyć naciśnij link: " + rescheduleLink;
        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta umówiona", message);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }

    private void sendDeleteNotification(Patient patientFromDb) {
        if (Objects.equals(patientFromDb.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patientFromDb.getEmail(),
                    "wizyta odwołana", "twoja wizyta została odwołana");
        } else {
            smsService.sendSms("+48"+patientFromDb.getPhoneNumber(), clinicPhoneNumber,
                    "twoja wizyta została odwołana");
        }
    }
}
