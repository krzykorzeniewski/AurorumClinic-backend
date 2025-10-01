package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final EmailService emailService;
    private final SmsService smsService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${twilio.trial_number}")
    private String clinicPhoneNumber;

    @TransactionalEventListener
    public void handleAppointmentCreatedEvent(AppointmentCreatedEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta została umówiona w dniu " + event.getAppointment().getStartedAt()
                .format(dateFormatter) + "\n" +
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

    @TransactionalEventListener
    public void handleAppointmentRescheduleEvent(AppointmentRescheduledEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta została przełożona na datę " + event.getAppointment().getStartedAt()
                .format(dateFormatter) + "\n" +
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

    @TransactionalEventListener
    public void handleAppointmentDeletedEvent(AppointmentDeletedEvent event) {
        Patient patient = event.getPatient();
        String message = "twoja wizyta w dniu " + event.getAppointment().getStartedAt()
                .format(dateFormatter) + " została anulowana";
        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta odwołana", message);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }


}
