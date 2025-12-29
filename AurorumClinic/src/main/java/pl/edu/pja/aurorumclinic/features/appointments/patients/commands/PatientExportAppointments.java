package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientExportAppointments {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> getAppointmentsExport(@AuthenticationPrincipal Long patientId) throws IOException {
        byte[] bytes = handle(patientId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "wizyty.ics" + "\"")
                .body(new ByteArrayResource(bytes));
    }

    private byte[] handle(Long patientId) throws IOException {
        patientRepository.findById(patientId).orElseThrow(
                () -> new ApiNotFoundException("Patient id not found", "id"));
        List<Appointment> patientFutureAppointments = appointmentRepository
                .findByPatient_IdAndFinishedAtAfter(patientId, LocalDateTime.now());


        Calendar icsCalendar = new Calendar()
                .withProdId("-//Aurorum Clinic//iCal4j 1.0//PL")
                .withDefaults()
                .getFluentTarget();
        RandomUidGenerator uidGenerator = new RandomUidGenerator();
        for (Appointment appointment : patientFutureAppointments) {
            VEvent appointmentCalEntry = new VEvent(appointment.getStartedAt(), appointment.getFinishedAt(),
                    appointment.getService().getName() + " "
                            + appointment.getDoctor().getName() + " "
                            + appointment.getDoctor().getSurname());
            appointmentCalEntry.add(uidGenerator.generateUid());
            icsCalendar.add(appointmentCalEntry);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(icsCalendar, outputStream);

        return outputStream.toByteArray();
    }

}
