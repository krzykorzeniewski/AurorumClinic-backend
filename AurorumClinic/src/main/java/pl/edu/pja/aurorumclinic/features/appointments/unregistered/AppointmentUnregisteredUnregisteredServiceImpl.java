package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class AppointmentUnregisteredUnregisteredServiceImpl implements AppointmentUnregisteredService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final AppointmentValidator appointmentValidator;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${mail.frontend.appointment.unregistered-delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.unregistered-reschedule-link}")
    private String rescheduleAppointmentLink;

    @Override
    public void createAppointmentForUnregisteredUser(CreateAppointmentUnregisteredRequest createRequest) {
        if (userRepository.findByEmail(createRequest.email()) != null) {
            throw new ApiConflictException("An account is registered for this email", "email");
        }
        Service serviceFromDb = serviceRepository.findById(createRequest.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(createRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        appointmentValidator.validateTimeSlot(createRequest.startedAt(),
                createRequest.startedAt().plusMinutes(serviceFromDb.getDuration()),
                doctorFromDb.getId(),
                serviceFromDb.getId());
        Appointment appointment = Appointment.builder()
                .doctor(doctorFromDb)
                .service(serviceFromDb)
                .startedAt(createRequest.startedAt())
                .finishedAt(createRequest.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .status(AppointmentStatus.CREATED)
                .description(createRequest.description())
                .build();
        Appointment savedAppointment = appointmentRepository.save(appointment);

        Guest guest = Guest.builder()
                .name(createRequest.name())
                .surname(createRequest.surname())
                .pesel(createRequest.pesel())
                .birthdate(createRequest.birthDate())
                .email(createRequest.email())
                .phoneNumber(createRequest.phoneNumber())
                .appointment(savedAppointment)
                .build();
        Guest savedGuest = guestRepository.save(guest);

        sendConfirmationEmail(savedGuest);
    }

    @Override
    public void deleteAppointmentForUnregisteredUser(String token) {
        Guest guestFromDb = guestRepository.findByAppointmentDeleteToken(token);
        if (guestFromDb == null) {
            throw new ApiNotFoundException("Token not found", "token");
        }
        if (guestFromDb.getAppointment().getStartedAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Appointment has already started", "token");
        }
        guestRepository.delete(guestFromDb);
    }

    @Override
    public void rescheduleAppointmentForUnregisteredUser(String token, RescheduleAppointmentUnregisteredRequest rescheduleRequest) {
        Guest guestFromDb = guestRepository.findByAppointmentRescheduleToken(token);
        if (guestFromDb == null) {
            throw new ApiNotFoundException("Token not found", "token");
        }
        if (guestFromDb.getAppointment().getStartedAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Appointment has already started", "token");
        }

        Appointment appointmentFromDb = guestFromDb.getAppointment();
        appointmentValidator.validateTimeSlot(rescheduleRequest.startedAt(),
                rescheduleRequest.startedAt().plusMinutes(appointmentFromDb.getService().getDuration()),
                appointmentFromDb.getDoctor().getId(),
                appointmentFromDb.getService().getId());

        appointmentFromDb.setStartedAt(rescheduleRequest.startedAt());
        appointmentFromDb.setFinishedAt(rescheduleRequest.startedAt()
                .plusMinutes(appointmentFromDb.getService().getDuration()));

        sendConfirmationEmail(guestFromDb);
    }

    private void sendConfirmationEmail(Guest guest) {
        String appointmentDeleteToken = tokenService.createRandomToken();
        guest.setAppointmentDeleteToken(appointmentDeleteToken);
        String deleteLink = deleteAppointmentLink + appointmentDeleteToken;

        String appointmentRescheduleToken = tokenService.createRandomToken();
        guest.setAppointmentRescheduleToken(appointmentRescheduleToken);
        String rescheduleLink = rescheduleAppointmentLink + appointmentRescheduleToken;

        emailService.sendEmail(
                noreplyEmailAddres, guest.getEmail(),
                "wizyta umówiona", "twoja wizyta została umówiona \n" +
                        "aby ją odwołać naciśnij link: " + deleteLink + "\n" +
                        "aby ją przełożyć naciśnij link: " + rescheduleLink);
    }
}
