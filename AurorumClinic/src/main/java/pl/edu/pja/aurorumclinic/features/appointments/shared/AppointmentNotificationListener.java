package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
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
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.frontend.appointment.delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.reschedule-link}")
    private String rescheduleAppointmentLink;

    @Value("${mail.backend.noreply-address}")
    private String noreplyEmailAddres;

    @Value("${twilio.trial_number}")
    private String clinicPhoneNumber;

    @TransactionalEventListener
    public void handleAppointmentCreatedEvent(AppointmentCreatedEvent event) {
        Appointment appointment = event.getAppointment();
        String rescheduleLink = rescheduleAppointmentLink + appointment.getId();
        String deleteLink = deleteAppointmentLink + appointment.getId();
        Patient patient = event.getPatient();

        Context context = new Context();
        context.setVariable("appointmentDate", appointment.getStartedAt().format(dateFormatter));
        context.setVariable("rescheduleLink", rescheduleLink);
        context.setVariable("deleteLink", deleteLink);
        String htmlPageAsText = springTemplateEngine.process("appointment-created-email", context);

        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta umówiona", htmlPageAsText);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    "wizyta umówiona: " + appointment.getStartedAt().format(dateFormatter));
        }
    }

    @TransactionalEventListener
    public void handleAppointmentRescheduleEvent(AppointmentRescheduledEvent event) {
        Appointment appointment = event.getAppointment();
        String rescheduleLink = rescheduleAppointmentLink + appointment.getId();
        String deleteLink = deleteAppointmentLink + appointment.getId();
        Patient patient = event.getPatient();

        Context context = new Context();
        context.setVariable("appointmentDate", appointment.getStartedAt().format(dateFormatter));
        context.setVariable("rescheduleLink", rescheduleLink);
        context.setVariable("deleteLink", deleteLink);
        String htmlPageAsText = springTemplateEngine.process("appointment-rescheduled-email", context);

        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta umówiona", htmlPageAsText);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    "wizyta umówiona: " + appointment.getStartedAt().format(dateFormatter)); //TODO add links to reschedule/delete in sms
        }
    }

    @TransactionalEventListener
    public void handleAppointmentDeletedEvent(AppointmentDeletedEvent event) {
        Patient patient = event.getPatient();
        Appointment appointment = event.getAppointment();

        Context context = new Context();
        context.setVariable("appointmentDate", appointment.getStartedAt().format(dateFormatter));
        String htmlPageAsText = springTemplateEngine.process("appointment-deleted-email", context);

        if (Objects.equals(patient.getCommunicationPreferences(), CommunicationPreference.EMAIL)) {
            emailService.sendEmail(
                    noreplyEmailAddres, patient.getEmail(),
                    "wizyta odwołana", htmlPageAsText);
        } else {
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    "wizyta odwołana: " + appointment.getStartedAt().format(dateFormatter));
        }
    }


}
