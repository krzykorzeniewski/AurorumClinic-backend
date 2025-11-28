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
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.test_config.IntegrationTest;
import pl.edu.pja.aurorumclinic.test_config.TestDataConfiguration;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomDoctor;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestDataConfiguration.class)
@RecordApplicationEvents
public class PatientCreateAppointmentIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockCustomDoctor //sets role to doctor inside the security context
    void createAppointmentShouldReturn403WhenRoleIsNotPatient() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now(),
                        2L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockCustomUser(id = 100) //non-existing patient id
    void createAppointmentShouldReturn404WhenPatientIdIsNotFound() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1),
                        2L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyText().containsIgnoringCase("patient");
    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldReturn404WhenDoctorIdIsNotFound() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1),
                        2L, 100L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyText().containsIgnoringCase("doctor");
    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldReturn404WhenServiceIdIsNotFound() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1),
                        200L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyText().containsIgnoringCase("service");
    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldReturn400WhenPayloadDataValidationFails() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1),
                        2L, null, "opis"); //notnull doctorid set to null

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("must not be null");

    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldReturn400WhenTimeSlotIsNotAvailable() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1), //appointment3 overlaps
                        2L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("timeslot");
    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldReturn400WhenServiceIsNotAssignedToDoctorSpecialization() throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(1),
                        1L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("specialization");
    }

    @Test
    @WithMockCustomUser
    void createAppointmentShouldPersistAppointmentWithRequestDataAndReturn200When(
            @Autowired ApplicationEvents applicationEvents
            ) throws JsonProcessingException {
        PatientCreateAppointment.PatientCreateAppointmentRequest request =
                new PatientCreateAppointment.PatientCreateAppointmentRequest(LocalDateTime.now().plusHours(2),
                        2L, 2L, "opis");

        MvcTestResult result = mvcTester.post().uri("/api/appointments/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(applicationEvents.stream(AppointmentCreatedEvent.class))
                .hasSize(1);
    }


}
