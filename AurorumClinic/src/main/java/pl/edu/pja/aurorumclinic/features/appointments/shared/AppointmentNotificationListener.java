package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.*;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.Survey;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.services.EmailService;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;
import pl.edu.pja.aurorumclinic.shared.services.SmsService;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

    private final EmailService emailService;
    private final SmsService smsService;
    private final ObjectStorageService objectStorageService;
    private final DateTimeFormatter dateFormatter;
    private final SpringTemplateEngine springTemplateEngine;

    @Value("${mail.frontend.appointment.delete-link}")
    private String deleteAppointmentLink;

    @Value("${mail.frontend.appointment.reschedule-link}")
    private String rescheduleAppointmentLink;

    @Value("${mail.frontend.appointment.survey-link}")
    private String appointmentSurveyLink;

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
            String message = String.format("""
                    Twoja wizyta została umówiona w dniu: %s
                    przełóż wizytę: %s
                    odwołaj wizytę: %s""", appointment.getStartedAt().format(dateFormatter), rescheduleLink, deleteLink);
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
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
            String message = String.format("""
                    Twoja wizyta została przełożona na dzień: %s
                    przełóż wizytę: %s
                    odwołaj wizytę: %s""", appointment.getStartedAt().format(dateFormatter), rescheduleLink, deleteLink);
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
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
            String message = String.format("""
                    Twoja wizyta z dnia: %s
                    została odwołana.""", appointment.getStartedAt().format(dateFormatter));
            smsService.sendSms("+48"+patient.getPhoneNumber(), clinicPhoneNumber,
                    message);
        }
    }

    @TransactionalEventListener
    public void handleSurveyCreatedEvent(SurveyCreatedEvent event) {
        Survey survey = event.survey();
        Appointment appointment = survey.getAppointment();
        Doctor doctor = survey.getAppointment().getDoctor();
        String profilePicture = objectStorageService.generateUrl(doctor.getProfilePicture());
        String surveyLink = appointmentSurveyLink + survey.getId();

        Context context = new Context();
        context.setVariable("appointmentDate", appointment.getStartedAt().format(dateFormatter));
        context.setVariable("doctorProfilePicture", profilePicture);
        context.setVariable("surveyLink", surveyLink);
        context.setVariable("doctorName", doctor.getName() +" " + doctor.getSurname());
        context.setVariable("doctorSpecialization", doctor.getSpecialization());

        String htmlPageAsText = springTemplateEngine.process("appointment-survey-email", context);
        emailService.sendEmail(
                noreplyEmailAddres, appointment.getPatient().getEmail(),
                "Oceń wizytę", htmlPageAsText);
    }

}
