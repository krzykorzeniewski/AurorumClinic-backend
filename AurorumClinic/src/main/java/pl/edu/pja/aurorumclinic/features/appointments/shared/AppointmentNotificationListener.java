package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.registered.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.features.appointments.unregistered.events.AppointmentUnregisteredCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.unregistered.events.AppointmentUnregisteredDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.unregistered.events.AppointmentUnregisteredRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Guest;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${twilio.trial_number}")
    private String clinicPhoneNumber;

    @EventListener
    public void handleAppointmentCreatedEvent(AppointmentCreatedEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta została umówiona w dniu " + event.getAppointment().getStartedAt() + "\n" +
                "aby ją odwołać naciśnij link: " + event.getDeleteLink() + "\n" +
                "aby ją przełożyć naciśnij link: " + event.getRescheduleLink();
        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta umówiona", message);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }

    @EventListener
    public void handleAppointmentRescheduleEvent(AppointmentRescheduledEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta została przełożona na datę " + event.getAppointment().getStartedAt() + "\n" +
                "aby ją odwołać naciśnij link: " + event.getDeleteLink() + "\n" +
                "aby ją przełożyć naciśnij link: " + event.getRescheduleLink();
        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta przełożona", message);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }

    @EventListener
    public void handleAppointmentDeletedEvent(AppointmentDeletedEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta w dniu " + event.getAppointment().getStartedAt() + " została anulowana";
        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta odwołana", message);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }

    @EventListener
    public void handleAppointmentUnregisteredDeletedEvent(AppointmentUnregisteredDeletedEvent event) {
        Guest guest = event.getGuest();
        String message = "twoja wizyta w dniu " + event.getAppointment().getStartedAt() + " została anulowana";
        emailService.sendEmail(
                noreplyEmailAddres, guest.getEmail(),
                "wizyta odwołana", message);
    }

    @EventListener
    public void handleAppointmentUnregisteredRescheduledEvent(AppointmentUnregisteredRescheduledEvent event) {
        Guest guest = event.getGuest();
        String message = "twoja wizyta została przełożona na datę " + event.getAppointment().getStartedAt() + "\n" +
                "aby ją odwołać naciśnij link: " + event.getDeleteLink() + "\n" +
                "aby ją przełożyć naciśnij link: " + event.getRescheduleLink();
        emailService.sendEmail(
                noreplyEmailAddres, guest.getEmail(),
                "wizyta przełożona", message);
    }

    @EventListener
    public void handeAppointmentUnregisteredCreatedEvent(AppointmentUnregisteredCreatedEvent event) {
        Guest guest = event.getGuest();
        String message = "twoja wizyta została umówiona w dniu " + event.getAppointment().getStartedAt() + "\n" +
                "aby ją odwołać naciśnij link: " + event.getDeleteLink() + "\n" +
                "aby ją przełożyć naciśnij link: " + event.getRescheduleLink();
        emailService.sendEmail(
                noreplyEmailAddres, guest.getEmail(),
                "wizyta umówiona", message);
    }

}
