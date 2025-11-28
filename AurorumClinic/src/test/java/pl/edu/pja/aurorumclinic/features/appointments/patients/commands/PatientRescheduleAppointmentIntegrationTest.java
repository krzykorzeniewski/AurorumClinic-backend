package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.test_config.IntegrationTest;
import pl.edu.pja.aurorumclinic.test_config.TestDataConfiguration;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomEmployee;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestDataConfiguration.class)
@RecordApplicationEvents
public class PatientRescheduleAppointmentIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AppointmentRepository appointmentRepository;


    @Test
    @WithMockCustomEmployee //not a patient
    void updateAppointmentShouldReturn403WhenUserRoleIsNotPatient() throws JsonProcessingException {
        Long appointmentId = 1L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
        new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(LocalDateTime.now().plusHours(5), "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockCustomUser
    void updateAppointmentShouldReturn404WhenAppointmentIdIsNotFound() throws JsonProcessingException {
        Long appointmentId = 100L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
                new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(LocalDateTime.now().plusHours(5), "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyText().containsIgnoringCase("appointment");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void updateAppointmentShouldReturn403WhenRequestPatientIdDoesNotMatchAppointmentPatientId() throws JsonProcessingException {
        Long appointmentId = 1L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
                new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(LocalDateTime.now().plusHours(5), "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).bodyText().containsIgnoringCase("denied");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void updateAppointmentShouldReturn400WhenTimeslotIsNotAvailable() throws JsonProcessingException {
        Long appointmentId = 3L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
                new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(LocalDateTime.now().plusHours(3), "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("timeslot");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void updateAppointmentShouldReturn400WhenPayloadDataValidationFails() throws JsonProcessingException {
        Long appointmentId = 3L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
                new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(null, "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("must not be null");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void updateAppointmentShouldUpdateAppointmentWithRequestDataAndReturn200WhenExistsForIdAndPatientId(
            @Autowired ApplicationEvents applicationEvents
            ) throws JsonProcessingException {
        Long appointmentId = 3L;
        PatientRescheduleAppointment.PatientUpdateAppointmentRequest request =
                new PatientRescheduleAppointment.PatientUpdateAppointmentRequest(LocalDateTime.now().plusHours(5),
                        "nowy opis");

        MvcTestResult result = mvcTester.put()
                .uri("/api/appointments/me/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);

        Appointment updatedAppointment = appointmentRepository.findById(appointmentId).orElseThrow();
        assertThat(updatedAppointment.getStartedAt()).isEqualToIgnoringNanos(request.startedAt());
        assertThat(updatedAppointment.getDescription()).isEqualTo(request.description());

        assertThat(applicationEvents.stream(AppointmentRescheduledEvent.class))
                .hasSize(1);
    }

}
