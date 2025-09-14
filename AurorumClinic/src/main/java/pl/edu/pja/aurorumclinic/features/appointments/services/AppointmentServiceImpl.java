package pl.edu.pja.aurorumclinic.features.appointments.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateAppointmentUnregisteredRequest;
import pl.edu.pja.aurorumclinic.features.appointments.repositories.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.repositories.GuestRepository;
import pl.edu.pja.aurorumclinic.features.appointments.repositories.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.auth.SecurityUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService{

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    @Value("${mail.frontend.appointment-delete-link}")
    private String deleteAppointmentLink;

    @Override
    @Transactional
    public void createAppointmentForUnregisteredUser(CreateAppointmentUnregisteredRequest createRequest) {
        Guest guest = Guest.builder()
                .name(createRequest.name())
                .surname(createRequest.surname())
                .pesel(createRequest.pesel())
                .birthdate(createRequest.birthDate())
                .email(createRequest.email())
                .phoneNumber(createRequest.phoneNumber())
                .build();
        Guest savedGuest = guestRepository.save(guest);
        Service serviceFromDb = serviceRepository.findById(createRequest.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(createRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Appointment appointment = Appointment.builder()
                .doctor(doctorFromDb)
                .service(serviceFromDb)
                .startedAt(createRequest.startedAt())
                .finishedAt(createRequest.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .status(AppointmentStatus.CREATED)
                .guest(savedGuest)
                .description(createRequest.description())
                .build();
        if (appointmentRepository.timeSlotExists(appointment.getStartedAt(), appointment.getFinishedAt(),
                appointment.getDoctor().getId(), appointment.getService().getId())) {
            Appointment savedAppointment = appointmentRepository.save(appointment);
            savedGuest.setAppointment(savedAppointment);

            String token = securityUtils.createRandomToken();
            savedGuest.setAppointmentDeleteToken(token);
            String link = deleteAppointmentLink + token;

            emailService.sendEmail(
                    "support@aurorumclinic.pl", savedGuest.getEmail(),
                    "wizyta umówiona", "twoja wizyta została umówiona \n" +
                            "aby ją odwołać naciśnij link: " + link + "\n");
                            // TODO "aby ją przełożyć naciśnij link " + );
        } else {
            throw new ApiException("Timeslot is not available", "appointment");
        }
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
}
