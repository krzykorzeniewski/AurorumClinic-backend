package pl.edu.pja.aurorumclinic.features.appointments.guests;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.services.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.features.appointments.guests.events.AppointmentGuestCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.guests.events.AppointmentGuestDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.guests.events.AppointmentGuestRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class AppointmentGuestServiceImpl implements AppointmentGuestService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final ServiceRepository serviceRepository;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AppointmentValidator appointmentValidator;

    @Value("${mail.frontend.appointment.unregistered-delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.unregistered-reschedule-link}")
    private String rescheduleAppointmentLink;

    @Override
    public void createAppointmentForUnregisteredUser(CreateAppointmentGuestRequest createRequest) {
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

        String appointmentDeleteToken = tokenService.createRandomToken();
        savedGuest.setAppointmentDeleteToken(appointmentDeleteToken);
        String deleteLink = deleteAppointmentLink + appointmentDeleteToken;

        String appointmentRescheduleToken = tokenService.createRandomToken();
        savedGuest.setAppointmentRescheduleToken(appointmentRescheduleToken);
        String rescheduleLink = rescheduleAppointmentLink + appointmentRescheduleToken;

        applicationEventPublisher.publishEvent(new AppointmentGuestCreatedEvent(savedGuest, savedAppointment,
                rescheduleLink, deleteLink));
    }

    @Override
    public void deleteAppointmentForUnregisteredUser(DeleteAppointmentGuestRequest deleteRequest) {
        Guest guestFromDb = guestRepository.findByAppointmentDeleteToken(deleteRequest.token());
        if (guestFromDb == null) {
            throw new ApiNotFoundException("Token not found", "token");
        }
        if (guestFromDb.getAppointment().getStartedAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Appointment has already started", "token");
        }
        guestRepository.delete(guestFromDb);
        applicationEventPublisher.publishEvent(
                new AppointmentGuestDeletedEvent(guestFromDb, guestFromDb.getAppointment()));
    }

    @Override
    public void rescheduleAppointmentForUnregisteredUser(RescheduleAppointmentGuestRequest rescheduleRequest) {
        Guest guestFromDb = guestRepository.findByAppointmentRescheduleToken(rescheduleRequest.token());
        if (guestFromDb == null) {
            throw new ApiNotFoundException("Token not found", "token");
        }
        if (guestFromDb.getAppointment().getStartedAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Appointment has already started", "token");
        }

        Appointment appointmentFromDb = guestFromDb.getAppointment();
        LocalDateTime newStartedAt = rescheduleRequest.startedAt();
        LocalDateTime newFinishedAt = newStartedAt.plusMinutes(appointmentFromDb.getService().getDuration());
        appointmentValidator.validateTimeSlot(newStartedAt, newFinishedAt, appointmentFromDb.getDoctor().getId(),
                appointmentFromDb.getService().getId());

        appointmentFromDb.setStartedAt(newStartedAt);
        appointmentFromDb.setFinishedAt(newFinishedAt);

        String appointmentDeleteToken = tokenService.createRandomToken();
        guestFromDb.setAppointmentDeleteToken(appointmentDeleteToken);
        String deleteLink = deleteAppointmentLink + appointmentDeleteToken;

        String appointmentRescheduleToken = tokenService.createRandomToken();
        guestFromDb.setAppointmentRescheduleToken(appointmentRescheduleToken);
        String rescheduleLink = rescheduleAppointmentLink + appointmentRescheduleToken;

        applicationEventPublisher.publishEvent(new AppointmentGuestRescheduledEvent(guestFromDb, appointmentFromDb,
                rescheduleLink, deleteLink));
    }
}
